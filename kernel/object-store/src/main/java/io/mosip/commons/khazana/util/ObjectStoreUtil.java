package io.mosip.commons.khazana.util;

import io.mosip.kernel.core.util.StringUtils;

public class ObjectStoreUtil {

    private static final String SEPARATOR = "/";

    public static String getName(String source, String process, String objectName) {
        String finalObjectName = "";
        if (StringUtils.isNotEmpty(source))
            finalObjectName = source + SEPARATOR;
        if (StringUtils.isNotEmpty(process))
            finalObjectName = finalObjectName + process + SEPARATOR;

        finalObjectName = finalObjectName + objectName;

        return finalObjectName;
    }
}
