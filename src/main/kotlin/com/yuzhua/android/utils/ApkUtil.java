package com.yuzhua.android.utils;

import android.content.res.AXmlResourceParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ApkUtil {

    public static ApkInfo readAPK(String apkPath) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkPath);
            Enumeration<?> enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                if (zipEntry.isDirectory()) {

                } else {
                    if ("androidmanifest.xml".equals(zipEntry.getName().toLowerCase())) {
                        AXmlResourceParser parser = new AXmlResourceParser();
                        InputStream is = zipFile.getInputStream(zipEntry);
                        parser.open(is);
                        ApkInfo ai = test(parser);
                        is.close();
                        parser.close();
                        return ai;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ApkInfo();
    }

    private static ApkInfo test(AXmlResourceParser parser) throws Exception {
        ApkInfo apkInfo = new ApkInfo();
        StringBuilder indent = new StringBuilder(10);
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return apkInfo;
            }

            switch (type) {
                case 0:
//                    log("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                case 1:
                default:
                    break;
                case 2:
//                    log("%s<%s%s", indent, getNamespacePrefix(parser.getPrefix()), parser.getName());
                    indent.append("\t");
                    int namespaceCountBefore = parser.getNamespaceCount(parser.getDepth() - 1);
                    int namespaceCount = parser.getNamespaceCount(parser.getDepth());

                    int i;
                    for (i = namespaceCountBefore; i != namespaceCount; ++i) {
//                        log("%sxmlns:%s=\"%s\"", indent, parser.getNamespacePrefix(i), parser.getNamespaceUri(i));
                    }

                    for (i = 0; i != parser.getAttributeCount(); ++i) {
//                        log("%s%s%s=\"%s\"", indent, getNamespacePrefix(parser.getAttributePrefix(i)), parser.getAttributeName(i), getAttributeValue(parser, i));
                        switch (parser.getName().toLowerCase()) {
                            case "manifest": {
                                if ("versionCode".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.setVersionCode(Integer.parseInt(getAttributeValue(parser, i)));
                                } else if ("versionName".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.setVersionName(getAttributeValue(parser, i));
                                } else if ("package".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.setApkPackage(getAttributeValue(parser, i));
                                }
                            }
                            case "uses-sdk": {
                                if ("minSdkVersion".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.setMinSdkVersion(getAttributeValue(parser, i));
                                } else if ("targetSdkVersion".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.setTargetSdkVersion(getAttributeValue(parser, i));
                                }
                            }
                            case "uses-permission": {
                                if ("name".equalsIgnoreCase(parser.getAttributeName(i))) {
                                    apkInfo.addUserPermission(getAttributeValue(parser, i));
                                }
                            }
                        }
                    }

//                    log("%s>", indent);
                    break;
                case 3:
                    indent.setLength(indent.length() - "\t".length());
//                    log("%s</%s%s>", indent, getNamespacePrefix(parser.getPrefix()), parser.getName());
                    break;
                case 4:
//                    log("%s%s", indent, parser.getText());
            }
        }
    }

    private static String getNamespacePrefix(String prefix) {
        return prefix != null && prefix.length() != 0 ? prefix + ":" : "";
    }

    private static String getAttributeValue(AXmlResourceParser parser, int index) {
        int type = parser.getAttributeValueType(index);
        int data = parser.getAttributeValueData(index);
        if (type == 3) {
            return parser.getAttributeValue(index);
        } else if (type == 2) {
            return String.format("?%s%08X", getPackage(data), data);
        } else if (type == 1) {
            return String.format("@%s%08X", getPackage(data), data);
        } else if (type == 4) {
            return String.valueOf(Float.intBitsToFloat(data));
        } else if (type == 17) {
            return String.format("0x%08X", data);
        } else if (type == 18) {
            return data != 0 ? "true" : "false";
        } else if (type == 5) {
            return Float.toString(complexToFloat(data)) + DIMENSION_UNITS[data & 15];
        } else if (type == 6) {
            return Float.toString(complexToFloat(data)) + FRACTION_UNITS[data & 15];
        } else if (type >= 28 && type <= 31) {
            return String.format("#%08X", data);
        } else {
            return type >= 16 && type <= 31 ? String.valueOf(data) : String.format("<0x%X, type 0x%02X>", data, type);
        }
    }

    private static String getPackage(int id) {
        return id >>> 24 == 1 ? "android:" : "";
    }

    private static void log(String format, Object... arguments) {
        System.out.printf(format, arguments);
        System.out.println();
    }

    public static float complexToFloat(int complex) {
        return (float) (complex & -256) * RADIX_MULTS[complex >> 4 & 3];
    }

    private static final float[] RADIX_MULTS = new float[]{0.00390625F, 3.051758E-5F, 1.192093E-7F, 4.656613E-10F};
    private static final String[] DIMENSION_UNITS = new String[]{"px", "dip", "sp", "pt", "in", "mm", "", ""};
    private static final String[] FRACTION_UNITS = new String[]{"%", "%p", "", "", "", "", "", ""};

}
