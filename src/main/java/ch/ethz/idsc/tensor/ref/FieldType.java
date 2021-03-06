// code by jph
package ch.ethz.idsc.tensor.ref;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ch.ethz.idsc.tensor.IntegerQ;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorFormat;
import ch.ethz.idsc.tensor.io.StringScalarQ;
import ch.ethz.idsc.tensor.qty.UnitSystem;
import ch.ethz.idsc.tensor.sca.Clip;

public enum FieldType {
  STRING(String.class::equals) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return string;
    }
  },
  BOOLEAN(Boolean.class::equals) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return BooleanParser.orNull(string);
    }
  },
  ENUM(Enum.class::isAssignableFrom) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return Stream.of(cls.getEnumConstants()) //
          .filter(object -> ((Enum<?>) object).name().equals(string)) //
          .findFirst() //
          .orElse(null);
    }
  },
  FILE(File.class::equals) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return new File(string);
    }
  },
  TENSOR(Tensor.class::equals) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return Tensors.fromString(string);
    }

    @Override
    public boolean isValidValue(Field field, Object object) {
      if (object instanceof Tensor && //
      !StringScalarQ.any((Tensor) object)) {
        Tensor tensor = (Tensor) object;
        {
          FieldColor fieldColor = field.getAnnotation(FieldColor.class);
          if (Objects.nonNull(fieldColor)) {
            try {
              ColorFormat.toColor(tensor);
            } catch (Exception exception) {
              return false;
            }
          }
        }
        return true;
      }
      return false;
    }
  },
  SCALAR(Scalar.class::equals) {
    @Override
    public Object toObject(Class<?> cls, String string) {
      return Scalars.fromString(string);
    }

    @Override
    public boolean isValidValue(Field field, Object object) {
      if (object instanceof Scalar) {
        Scalar scalar = (Scalar) object;
        if (StringScalarQ.of(scalar))
          return false;
        {
          FieldIntegerQ fieldIntegerQ = field.getAnnotation(FieldIntegerQ.class);
          if (Objects.nonNull(fieldIntegerQ))
            if (!IntegerQ.of(scalar))
              return false;
        }
        {
          FieldClip fieldClip = field.getAnnotation(FieldClip.class);
          if (Objects.nonNull(fieldClip)) {
            Clip clip = TensorReflection.clip(fieldClip);
            try {
              if (clip.isOutside(UnitSystem.SI().apply(scalar)))
                return false;
            } catch (Exception exception) {
              // System.err.println("unit incompatible " + clip + " " + scalar);
              return false;
            }
          }
        }
        return true;
      }
      return false;
    }
  }, //
  ;

  private final Predicate<Class<?>> predicate;

  private FieldType(Predicate<Class<?>> predicate) {
    this.predicate = predicate;
  }

  /* package */ final boolean isTracking(Class<?> cls) {
    return predicate.test(cls);
  }

  /* package */ abstract Object toObject(Class<?> cls, String string);

  public boolean isValidString(Field field, String string) {
    return isValidValue(field, toObject(field.getClass(), string));
  }

  public boolean isValidValue(Field field, Object object) {
    return Objects.nonNull(object) //
        && predicate.test(object.getClass()); // default implementation
  }

  /* package */ static String toString(Class<?> cls, Object object) {
    return Enum.class.isAssignableFrom(cls) //
        ? ((Enum<?>) object).name()
        : object.toString();
  }
}