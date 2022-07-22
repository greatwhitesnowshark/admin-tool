package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utilities {

    public static class Triple<A, B, C> {
        public A pFirst;
        public B pSecond;
        public C pThird;
        public Triple(A pFirst, B pSecond, C pThird) {
            this.pFirst = pFirst;
            this.pSecond = pSecond;
            this.pThird = pThird;
        }
    }

    public static class Pair<D, E> {
        public D pFirst;
        public E pSecond;
        public Pair(D pFirst, E pSecond) {
            this.pFirst = pFirst;
            this.pSecond = pSecond;
        }
    }

    public static String JoinString(String[] aSplit, int nStart) {
        return JoinString(aSplit, nStart, " ");
    }

    public static String JoinString(byte[] aSplit, boolean bTransform, String sSeparator) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < aSplit.length; i++) {
            if (bTransform) {
                str.append((char) aSplit[i]);
            } else {
                str.append(aSplit[i]);
            }
            if (i != aSplit.length - 1) {
                str.append(sSeparator);
            }
        }
        return str.toString();
    }

    public static String JoinByteString(byte[] aSplit) {
        return JoinString(aSplit, false, ", ");
    }

    public static String JoinString(String[] aSplit, int nStart, String sSeparator) {
        StringBuilder str = new StringBuilder();
        for (int i = nStart; i < aSplit.length; i++) {
            str.append(aSplit[i]);
            if (i != aSplit.length - 1) {
                str.append(sSeparator);
            }
        }
        return str.toString();
    }

    public static String AddStringPaddingTabbed(String sText, int nTotalLen) {
        StringBuilder sBuilder = new StringBuilder();
        if (sText == null) {
            sText = "";
        }
        if (!sText.isBlank()) {
            sBuilder.append(sText);
        }
        int nRemainingLen = nTotalLen - sBuilder.length();
        if (nRemainingLen > 0) {
            if (nRemainingLen >= 4) {
                nRemainingLen /= 4;
            } else {
                if (nRemainingLen >= 2) {
                    nRemainingLen = 1;
                } else {
                    nRemainingLen = 0;
                }
            }
            for (int a = 0; a < nRemainingLen; a++) {
                sBuilder.append("\t");
            }
        }
        return sBuilder.toString();
    }

    public static String AddStringPadding(String sText, int nTotalLen) {
        StringBuilder sBuilder = new StringBuilder();
        int i = 0;
        if (sText == null) {
            sText = "";
        }
        for (char c : sText.toCharArray()) {
            sBuilder.append(c);
            i++;
            if (i >= nTotalLen) {
                break;
            }
        }
        if (i < nTotalLen) {
            for (int a = 0; a < (nTotalLen - i); a++) {
                sBuilder.append(String.format("%s", " "));
            }
        }
        return sBuilder.toString();
    }

    public static int CountMatches(String sText, char cFind) {
        int nCount = 0;
        for (int i = 0; i < sText.length(); i++) {
            if (sText.charAt(i) == cFind) {
                nCount++;
            }
        }
        return nCount;
    }
    
    public static void main(String[] args)

    {
        int[] aDamageSkin = {
                2439157,  // Abrup's Snowstorm Damage Skin (Skin-ID: 1345)
                2436679,  // Arcana Damage Skin (Skin-ID: 161)
                2436808,  // Aspire Industries Damage Skin (Skin-ID: 1260)
                2433267,  // Blood Damage Skin (Skin-ID: 1002)
                2438302,  // Cadena Damage Skin (Skin-ID: 176)
                2436085,  // Chestnut Damage Skin (Skin-ID: 135)
                2436742,  // Chinese Text Damage Skin (Skin-ID: 1251)
                2436741,  // Chinese Text Damage Skin (30-day) (Skin-ID: 1251)
                2438238,  // Choco Donut Damage Skin (Skin-ID: 104)
                2437168,  // Crayon Damage Skin (Skin-ID: 1276)
                2434157,  // Damien's Band Damage Skin (Skin-ID: 1043)
                2435543,  // Epic Lulz Damage Skin (Skin-ID: 1090)
                2438415,  // Esfera Damage Skin (Skin-ID: 197)
                2438467,  // Graffiti Damage Skin (Skin-ID: 1322)
                2436212,  // Hallowkitty Damage Skin (Skin-ID: 143)
                2434545,  // Hayato Damage Skin (Skin-ID: 1054)
                2435565,  // Heroes Aran Damage Skin (Skin-ID: 1095)
                2435567,  // Heroes Evan Damage Skin (Skin-ID: 1097)
                2435566,  // Heroes Luminous Damage Skin (Skin-ID: 1096)
                2436042,  // Heroes Mercedes Damage Skin (Skin-ID: 78)
                2436041,  // Heroes Phantom Damage Skin (Skin-ID: 78)
                2435568,  // Heroes Shade Damage Skin (Skin-ID: 1094)
                2434147,  // Irena's Band Damage Skin (Skin-ID: 1042)
                2434662,  // Jelly Bean Damage Skin (Skin-ID: 1058)
                2434544,  // Kanna Damage Skin (Skin-ID: 1053)
                2439381,  // Master Stellar Damage Skin (Skin-ID: 213)
                2433709,  // Moon Bunny Damage Skin (Skin-ID: 91)
                /* this one, Moon Bunny Damage Skin ^^^ applies with the wrong nDamageSkinID */
                2433214,  // Noise Damage Skin (Skin-ID: 1017)
                2436831,  // Petal Damage Skin (Skin-ID: 1262)
                2438272,  // Pew Pew Damage Skin (Skin-ID: 144)
                2436653,  // Reverse Damage Skin (Skin-ID: 1239)
                2436746,  // Roman Numeral Damage Skin (Skin-ID: 1257)
                2433183,  // Super Spooky Damage Skin (Skin-ID: 1016)
                2438265,  // Twilight Damage Skin (Skin-ID: 136)
                2438266,  // Unyielding Fury Damage Skin (Skin-ID: 137)
                2435955,  // Wandering Soul Damage Skin (Skin-ID: 1125)
                2433829,  // White Heaven Rain Damage Skin (Skin-ID: 1035)
                2436474,  // XOXO Damage Skin (Skin-ID: 1233)
                2434601,  // Invisible Damage Skin (Skin-ID: 1051)
                2435193,  // Krakian Damage Skin (Skin-ID: 1080)
                2433182,  // Jack o' Lantern Damage Skin (Skin-ID: 1019)
                2433268,  // Zombie Damage Skin (Skin-ID: 1004)
                2433081,  // Halloween Damage Skin (Skin-ID: 1007)
                2433184,  // Wicked Witch Damage Skin (Skin-ID: 1018)
                2435948,  // Halloween Town Damage Skin (Skin-ID: 1119)
                2435949 // Too Spooky Damage Skin (Skin-ID: 1120)
        };
        int[] aVPDamageSkin = {2431965, 2431966, 2431967, 2432131, 2432153, 2432154, 2432207, 2432354, 2432355, 2432465, 2432479, 2432526, 2432592, 2432640, 2432710, 2432836, 2432973, 2433063, 2433178, 2433456, 2435956, 2433715, 2433804, 2433913, 2433980, 2433981, 2436229, 2434248, 2433362, 2434274, 2434289, 2434390, 2434391, 2434528, 2434529, 2434530, 2433571, 2434574, 2433828, 2432804, 2434654, 2435326, 2432749, 2434710, 2433777, 2434824, 2434662, 2434664, 2434868, 2436041, 2436042, 2435046, 2435047, 2435836, 2435141, 2435179, 2435162, 2435157, 2435835, 2435159, 2436044, 2434663, 2435182, 2435850, 2435184, 2435222, 2435293, 2435313, 2435331, 2435332, 2435333, 2435334, 2435316, 2435408, 2435427, 2435428, 2435429, 2435456, 2435493, 2435959, 2435958, 2435431, 2435430, 2435432, 2435433, 2435521, 2435196, 2435523, 2435524, 2435538, 2435832, 2435833, 2435839, 2435840, 2435841, 2435849, 2435972, 2436023, 2436024, 2436026, 2436027, 2436028, 2436029, 2436045};

        for (int i = 0; i < aDamageSkin.length; i++) {
            int nDamageSkinID = aDamageSkin[i];
            for (int a : aVPDamageSkin) {
                if (a == nDamageSkinID) {
                    System.out.println("Match:  " + a);
                }
            }
        }
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> SortedMapByValue(Map<K, V> map, boolean bAscending) {
        List<Map.Entry<K, V>> lEntry = new ArrayList<>(map.entrySet());
        Map<K, V> mResult;
        if (!bAscending) {
            lEntry.sort(Map.Entry.comparingByValue());
            mResult = new LinkedHashMap<>();
            for (int i = lEntry.size() - 1; i >= 0; i--) {
                Map.Entry<K, V> entry = lEntry.get(i);
                mResult.put(entry.getKey(), entry.getValue());
            }
        } else {
            lEntry.sort(Map.Entry.comparingByValue());
            mResult = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : lEntry) {
                mResult.put(entry.getKey(), entry.getValue());
            }
        }
        return mResult;
    }

    public static <K extends Comparable, V extends Comparable<? super V>> Map<K, V> SortedMapByKey(Map<K, V> map, boolean bAscending) {
        List<Map.Entry<K, V>> lEntry = new ArrayList<>(map.entrySet());
        Map<K, V> mResult;
        if (!bAscending) {
            lEntry.sort(Map.Entry.comparingByKey());
            mResult = new LinkedHashMap<>();
            for (int i = lEntry.size() - 1; i >= 0; i--) {
                Map.Entry<K, V> entry = lEntry.get(i);
                mResult.put(entry.getKey(), entry.getValue());
            }
        } else {
            lEntry.sort(Map.Entry.comparingByKey());
            mResult = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : lEntry) {
                mResult.put(entry.getKey(), entry.getValue());
            }
        }
        return mResult;
    }
}
