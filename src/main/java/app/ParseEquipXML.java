package app;

import root.User;
import root.database.game.user.inventory.item.ItemSlotEquip;
import root.database.passport.Account;
import util.FileTime;
import util.Utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseEquipXML {

    public static String sAccountName = "Deawon";
    public static String sAuctionFile = "C:\\Users\\Chris\\Desktop\\Auction.txt";
    public static String sFile = "C:\\Users\\Chris\\Desktop\\sqlbackups\\resultset\\equips\\";
    public static String sOutputFile = "C:\\Users\\Chris\\Desktop\\sqlbackups\\resultset\\results\\";
    public static String sPotentialFile = "C:\\Users\\Chris\\Desktop\\sqlbackups\\resultset\\potential.txt";
    public static String sItemInfoFile = "C:\\Users\\Chris\\Desktop\\sqlbackups\\resultset\\itemid.txt";
    public static Map<Integer, String> mPotentialInfo = new HashMap<>();
    public static Map<Integer, String> mItemInfo = new HashMap<>();
    public static List<String> lAuctionName = new LinkedList<>();
    public static List<String> lFieldNames = Arrays.stream(ItemSlotEquip.class.getDeclaredFields()).map((f) -> f.getName()).collect(Collectors.toList());

    public static void main(String[] args) {
        //LoadAuctionIDs();
        lAuctionName.add(sAccountName);
        System.out.println("Auction Account Names:  \r\n" + Utilities.JoinString(lAuctionName.toArray(new String[0]), 0, ", "));
        LoadItemData();
        LoadPotentialData();
        for (String sAuctionName : lAuctionName) {
            List<ItemSlotEquip> lItem = new LinkedList<>();
            String sFileOut = sFile + sAuctionName + ".xml";
            System.out.println(sFileOut);
            try (BufferedReader br = new BufferedReader(new FileReader(sFileOut))) {
                while (br.ready()) {
                    String sLine = br.readLine();
                    if (!sLine.isBlank()) {
                        if (sLine.contains("<row>")) {
                            ItemSlotEquip i = new ItemSlotEquip(0, false);
                            while (br.ready()) {
                                String s = br.readLine();
                                if (!s.isBlank() && s.contains("</row>")) {
                                    lItem.add(i);
                                    break;
                                }
                                if (!s.isBlank() && s.contains("field name=")) {
                                    String sField = s.substring(s.indexOf("\"") + 1, s.indexOf(">") - 1);
                                    if (!sField.isBlank() && lFieldNames.contains(sField)) {
                                        Field f = i.getClass().getDeclaredField(sField);
                                        if (f != null) {
                                            String sValue = s.substring(s.indexOf(">") + 1);
                                            sValue = sValue.substring(0, sValue.indexOf("<"));
                                            Class<?> type = f.getType();
                                            if (type == int.class) {
                                                f.set(i, Integer.parseInt(sValue));
                                            } else if (type == long.class) {
                                                f.set(i, Long.parseLong(sValue));
                                            } else if (type == short.class) {
                                                f.set(i, Short.parseShort(sValue));
                                            } else if (type == byte.class) {
                                                f.set(i, Byte.parseByte(sValue));
                                            } else if (type == String.class) {
                                                f.set(i, sValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            String sOutFile = sOutputFile + sAuctionName + ".txt";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(sOutFile))) {
                Account pUser = new Account(sAuctionName);
                bw.write("\r\n=====================================\r\nAccount Trace\r\n=====================================\r\n");
                for (Field f : Account.class.getDeclaredFields()) {
                    if (f.getName().contains("sSPW") || f.getName().contains("sPassword")) {
                        continue;
                    }
                    try {
                        Object o = f.get(pUser);
                        if (o != null) {
                            if (o instanceof Number || f.getType().isPrimitive()) {
                                Class<?> type = f.getType();
                                long nVal = 0;
                                if (type == int.class) {
                                    nVal = (Integer) o;
                                } else if (type == long.class) {
                                    nVal = (Long) o;
                                    if (nVal == 3439756800000L) {
                                        continue;
                                    }
                                    if (f.getName().contains("ftRegisterDate") || f.getName().contains("ftExpire")) {
                                        String sOut = "\t\t" + Utilities.AddStringPadding("`" + f.getName() + "`", 24) + " -> " + new FileTime(nVal).toString();
                                        System.out.println(sOut);

                                        bw.write(sOut);
                                        bw.newLine();
                                        continue;
                                    }
                                } else if (type == short.class) {
                                    nVal = (Short) o;
                                } else if (type == byte.class) {
                                    nVal = (Byte) o;
                                }
                                if (nVal <= 0) {
                                    continue;
                                }
                            } else if (o instanceof String) {
                                String sVal = (String) o;
                                if (sVal.isBlank() || sVal.equalsIgnoreCase("null")) {
                                    continue;
                                }
                            }
                            String sOut = "\t\t" + Utilities.AddStringPadding("`" + f.getName() + "`", 24) + " -> " + o;
                            System.out.println(sOut);

                            bw.write(sOut);
                            bw.newLine();
                        }
                    } catch (Exception ignore) {
                    }
                }
                bw.write("\r\n=====================================\r\nEquip Trace\r\n=====================================\r\n");
                bw.write("\r\nTotal # of loaded items = " + lItem.size());
                int nCount = 0;
                for (ItemSlotEquip i : lItem) {
                    nCount++;
                    bw.newLine();
                    bw.newLine();
                    if (i.sCharacterName == null) {
                        bw.write("[Trunk Equip Item]\r\n");
                    } else {
                        bw.write("Character: `" + i.sCharacterName + "` (" + i.dwCharacterID + ")\r\n");
                    }
                    bw.write("Account:   `" + i.sAccountClubID + "` (" + i.dwAccountID + ")\r\n");
                    bw.write("\r\n\tEquip Item #" + nCount + "\r\n\r\n");
                    for (Field f : i.getClass().getDeclaredFields()) {
                        try {
                            if (f.getName().contains("nItemState") || f.getName().contains("dwAccountID") || f.getName().contains("dwCharacterID") || f.getName().contains("nExGradeOption") || f.getName().contains("sCharacterName") || f.getName().contains("sAccountClubID")) {
                                continue;
                            }
                            Object o = f.get(i);
                            if (o != null) {
                                if (o instanceof Number || f.getType().isPrimitive()) {
                                    Class<?> type = f.getType();
                                    long nVal = 0;
                                    if (type == int.class) {
                                        nVal = (Integer) o;
                                    } else if (type == long.class) {
                                        nVal = (Long) o;
                                        if (nVal == 3439756800000L) {
                                            continue;
                                        }
                                        if (f.getName().contains("ftRegisterDate") || f.getName().contains("ftExpire")) {
                                            String sOut = "\t\t" + Utilities.AddStringPadding("`" + f.getName() + "`", 24) + " -> " + new FileTime(nVal).toString();
                                            System.out.println(sOut);

                                            bw.write(sOut);
                                            bw.newLine();
                                            continue;
                                        }
                                    } else if (type == short.class) {
                                        nVal = (Short) o;
                                    } else if (type == byte.class) {
                                        nVal = (Byte) o;
                                    }
                                    if (nVal <= 0) {
                                        continue;
                                    }
                                } else if (o instanceof String) {
                                    String sVal = (String) o;
                                    if (sVal.isBlank() || sVal.equalsIgnoreCase("null")) {
                                        continue;
                                    }
                                }
                                if (f.getName().contains("nItemID")) {
                                    String sItemName = "\t\t" + Utilities.AddStringPadding("`ItemName`", 24) + " -> " + mItemInfo.get((Integer) o);
                                    System.out.println(sItemName);

                                    bw.write(sItemName);
                                    bw.newLine();
                                } else if (f.getName().contains("nOption")) {
                                    o = (String) mPotentialInfo.get((Integer) o);
                                }
                                String sOut = "\t\t" + Utilities.AddStringPadding("`" + f.getName() + "`", 24) + " -> " + o;
                                System.out.println(sOut);

                                bw.write(sOut);
                                bw.newLine();
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void LoadAuctionIDs() {
        try (BufferedReader br = new BufferedReader(new FileReader(sAuctionFile))) {
            while (br.ready()) {
                String sLine = br.readLine();
                if (!sLine.isBlank()) {
                    String sID = sLine.substring(0, sLine.indexOf(" "));
                    if (!sID.isBlank()) {
                        int dwCharID = User.GetCharIDFromCharName(sID.trim());
                        int dwAccID = User.GetAccountIDFromCharID(dwCharID);
                        String sAccName = User.GetAccountNameFromAccountID(dwAccID);
                        if (!sAccName.isBlank() && !lAuctionName.contains(sAccName)) {
                            lAuctionName.add(sAccName);
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    public static void LoadItemData() {
        try (BufferedReader br = new BufferedReader(new FileReader(sItemInfoFile))) {
            while (br.ready()) {
                String sLine = br.readLine();
                if (!sLine.isBlank()) {
                    String sID = sLine.substring(0, sLine.indexOf(" // "));
                    String sName = sLine.substring(sLine.indexOf(" // ") + 4);
                    if (!sID.isBlank() && !sName.isBlank()) {
                        mItemInfo.put(Integer.parseInt(sID), sName);
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    public static void LoadPotentialData() {
        try (BufferedReader br = new BufferedReader(new FileReader(sPotentialFile))) {
            while (br.ready()) {
                String sLine = br.readLine();
                if (!sLine.isBlank()) {
                    String sID = sLine.substring(0, sLine.indexOf(":"));
                    String sName = sLine.substring(sLine.indexOf(":") + 2);
                    if (sName.contains("#time")) {
                        sName = sName.replace("#time", "<time>");
                    }
                    if (!sID.isBlank() && !sName.isBlank()) {
                        String sMatchGroup1 = "(.*)#(\\w+)(%|\\s)(.*)";
                        String sMatchGroup2 = "\\((\\d{1,4}.*)\\)";
                        sName = sName.replaceAll(sMatchGroup1 + sMatchGroup2, "$1$5$3$4");
                        mPotentialInfo.put(Integer.parseInt(sID), sName);
                        System.out.println("Loaded potential info:  " + sID + " - " + sName);
                    }
                }
            }
        } catch (Exception ignore) {}
    }
}
