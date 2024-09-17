package org.threefour.ddip.util;

import org.threefour.ddip.exception.InvalidTargetTypeException;
import org.threefour.ddip.exception.ParsingIntegerException;
import org.threefour.ddip.exception.ParsingLongException;
import org.threefour.ddip.exception.ParsingShortException;
import org.threefour.ddip.image.domain.TargetType;

import static org.threefour.ddip.exception.ExceptionMessage.*;

public class FormatConverter {
    public static long parseToLong(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingLongException((String.format(PARSING_LONG_EXCEPTION_MESSAGE, number)));
        }
    }

    public static int parseToInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingIntegerException((String.format(PARSING_INTEGER_EXCEPTION_MESSAGE, number)));
        }
    }

    public static short parseToShort(String number) throws ParsingShortException {
        try {
            return Short.parseShort(number);
        } catch (NumberFormatException nfe) {
            throw new ParsingShortException((String.format(PARSING_SHORT_EXCEPTION_MESSAGE, number)));
        }
    }

    public static TargetType parseToTargetType(String targetType) {
        try {
            return TargetType.valueOf(targetType);
        } catch (IllegalArgumentException iae) {
            throw new InvalidTargetTypeException(String.format(INVALID_TARGET_TYPE_EXCEPTION_MESSAGE, targetType));
        }
    }
}
