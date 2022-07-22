package app;

import database.Config;
import root.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class AdminTool {

    public static AdminTool pInstance;
    public static boolean bIsLoading = false;
    public static int dwSelectedUser = 0;
    private Map<Integer, ListTypes> mListOutput;
    private Map<Integer, User> mUserHistory = new HashMap<>();
    private List<Integer> lUserComboBox = new LinkedList<>();
    private JPanel pPanel;
    private JComboBox pSearchTypesBox;
    private JTextField pSearchBarTextField;
    private JButton pSearchButton;
    private JCheckBox pLogAccountCheckBox, pLogDonateCheckBox, pLogAuctionCheckBox, pCharactersCheckBox, pInventoryCheckBox,
            pEquipItemsCheckBox, pLockerCheckBox, pSearchFullCheckBox;
    private JList pResultsList;
    private JCheckBox pSelectAllCheckBox;
    private JLabel pTitleLabel, pStatusLabel;
    private JComboBox pUserComboBox;
    private JTable pDataTable;
    private JScrollPane pTableScrollPane;
    private ListTypes pCurrentView = null;
    private int nSelection = -1;
    private boolean bSelectAll;

    public enum SearchTypes {
        AccountUsername, AccountID, CharacterName, CharacterID;

        public static SearchTypes GetByIndex(int nIndex) {
            return values()[nIndex];
        }
    }

    public AdminTool() {
        InitCheckBoxes();
        InitButtons();
        InitList();
        InitTable();
        InitComboBox();
        pInstance = this;
    }

    public void AddToUserComboBox(int dwAccountID, String sAccountClubID, boolean bSelected) {
        if (!lUserComboBox.contains(dwAccountID)) {
            lUserComboBox.add(dwAccountID);
            pUserComboBox.addItem(sAccountClubID);
            if (bSelected) {
                pUserComboBox.setSelectedItem(sAccountClubID);
            }
            pUserComboBox.updateUI();
            //UpdateList();
        }
    }

    public void InitTable() {
        pDataTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Account ID", "HWID/IP Match", "No. of Matches"}));
    }

    public void InitComboBox() {
        pUserComboBox.addActionListener(l -> {
            User pUser = User.GetUser((String) pUserComboBox.getSelectedItem());
            if (pUser != null) {
                SetTableData(pUser, pCurrentView);
            }
        });
    }

    public void InitList() {
        ListSelectionModel pModel = pResultsList.getSelectionModel();
        pModel.addListSelectionListener(e -> {
            ListSelectionModel lModel = (ListSelectionModel) e.getSource();
            int nChanged = nSelection;
            int nMin = lModel.getMinSelectionIndex();
            int nMax = lModel.getMaxSelectionIndex();
            if (lModel.isSelectedIndex(nMin)) {
                nSelection = nMin;
            } else if (lModel.isSelectedIndex(nMax)) {
                nSelection = nMax;
            } else {
                return;
            }
            if (nSelection != nChanged) {
                ListTypes l = mListOutput.get(nSelection);
                if (l != null) {
                    pCurrentView = l;
                    User pUser = mUserHistory.get(dwSelectedUser);
                    if (pUser != null) {
                        SetTableData(pUser, l);
                    }
                }
            }
        });
    }

    public void SetTableData(User pUser, ListTypes l) {
        DefaultTableModel pModel = new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Account ID", "HWID/IP Match", "No. of Matches"});
        if (l != null) {
            pDataTable.setModel(pUser.mTableData.getOrDefault(l, pModel));
        } else {
            pDataTable.setModel(pModel);
        }
    }

    public void SetStatusLabel(String sStatus) {
        pStatusLabel.setText(sStatus);
        pStatusLabel.updateUI();
    }

    public void InitButtons() {
        pSearchButton.addActionListener(e -> {
            String sFind = pSearchBarTextField.getText();
            if (!bIsLoading) {
                bIsLoading = true;
                pDataTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Account ID", "HWID/IP Match", "No. of Matches"}));
                SetStatusLabel("Status => Searching/Compiling");
                if (sFind != null && !sFind.isBlank()) {
                    CompletableFuture.runAsync(() -> {
                        User pUser = null;
                        int nIndex = pSearchTypesBox.getSelectedIndex();
                        if (nIndex >= 0 && nIndex < SearchTypes.values().length) {
                            SearchTypes pType = SearchTypes.GetByIndex(nIndex);
                            switch (pType) {
                                case AccountUsername:
                                    pUser = User.GetUser(sFind);
                                    break;
                                case AccountID:
                                    pUser = User.GetUser(Integer.parseInt(sFind));
                                    break;
                                case CharacterName: {
                                    int dwCharacterID = User.GetCharIDFromCharName(sFind);
                                    int dwAccountID = User.GetAccountIDFromCharID(dwCharacterID);
                                    if (dwAccountID > 0) {
                                        pUser = User.GetUser(dwAccountID);
                                    }
                                    break;
                                }
                                case CharacterID: {
                                    int dwAccountID = User.GetAccountIDFromCharID(Integer.parseInt(sFind));
                                    if (dwAccountID > 0) {
                                        pUser = User.GetUser(dwAccountID);
                                    }
                                    break;
                                }
                            }
                            if (pUser != null) {
                                if (!mUserHistory.containsKey(pUser.dwAccountID)) {
                                    mUserHistory.put(pUser.dwAccountID, pUser);
                                }
                                dwSelectedUser = pUser.dwAccountID;
                                if (!lUserComboBox.contains(dwSelectedUser)) {
                                    pUserComboBox.addItem(pUser.sAccountName);
                                    lUserComboBox.add(dwSelectedUser);
                                }
                                pUserComboBox.setSelectedItem(pUser.sAccountName);
                                pStatusLabel.setText("Status: Idle");
                                bIsLoading = false;
                                UpdateList();
                                SetTableData(pUser, pCurrentView);
                            }
                        }
                    });
                }
            }
        });
    }

    public void InitCheckBoxes() {
        pLogAccountCheckBox.addActionListener(e -> Config.LOG_FULL_LOG_ACCOUNT = !Config.LOG_FULL_LOG_ACCOUNT);
        pLogDonateCheckBox.addActionListener(e -> Config.LOG_DONATE = !Config.LOG_DONATE);
        pCharactersCheckBox.addActionListener(e -> Config.LOG_CHARACTER_LIST = !Config.LOG_CHARACTER_LIST);
        pInventoryCheckBox.addActionListener(e -> Config.LOG_INVENTORY = !Config.LOG_INVENTORY);
        pEquipItemsCheckBox.addActionListener(e -> Config.LOG_EQUIPS = !Config.LOG_EQUIPS);
        pLockerCheckBox.addActionListener(e -> {
            Config.LOG_LOCKER = !Config.LOG_LOCKER;
            Config.LOG_TRUNK = Config.LOG_LOCKER;
        });
        pLogAuctionCheckBox.addActionListener(e -> Config.LOG_AUCTION = !Config.LOG_AUCTION);
        pSearchFullCheckBox.addActionListener(e -> Config.SEARCH_WITH_HWID_IP = !Config.SEARCH_WITH_HWID_IP);
        pSelectAllCheckBox.addActionListener(e -> {
            bSelectAll = !bSelectAll;
            boolean bSetSelected = !Config.LOG_FULL_LOG_ACCOUNT || !bSelectAll;
            if (bSetSelected) {
                pLogAccountCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_DONATE || !bSelectAll;
            if (bSetSelected) {
                pLogDonateCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_CHARACTER_LIST || !bSelectAll;
            if (bSetSelected) {
                pCharactersCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_INVENTORY || !bSelectAll;
            if (bSetSelected) {
                pInventoryCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_EQUIPS || !bSelectAll;
            if (bSetSelected) {
                pEquipItemsCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_LOCKER || !bSelectAll;
            if (bSetSelected) {
                pLockerCheckBox.setSelected(true);
            }
            bSetSelected = !Config.LOG_AUCTION || !bSelectAll;
            if (bSetSelected) {
                pLogAuctionCheckBox.setSelected(true);
            }
            bSetSelected = !Config.SEARCH_WITH_HWID_IP || !bSelectAll;
            if (bSetSelected) {
                pSearchFullCheckBox.setSelected(true);
            }
            Config.LOG_FULL_LOG_ACCOUNT =
                    Config.LOG_DONATE =
                            Config.LOG_CHARACTER_LIST =
                                    Config.LOG_INVENTORY =
                                            Config.LOG_EQUIPS =
                                                    Config.LOG_LOCKER =
                                                            Config.LOG_AUCTION =
                                                                    Config.SEARCH_WITH_HWID_IP =
                                                                            bSelectAll;
        });
    }

    public void UpdateList() {
        Vector<String> v = new Vector<>();
        int nCount = 0;

        mListOutput = new LinkedHashMap<>();

        for (ListTypes pType : ListTypes.values()) {
            if (pType.IsVisible()) {
                v.add(pType.GetTitle());
                mListOutput.put(nCount++, pType);
            }
        }

        pResultsList.setListData(v);
        pResultsList.updateUI();
    }

    public JPanel GetPanel() {
        return this.pPanel;
    }


    public enum ListTypes {

        AccountDetails("Account Details", (NotUsed) -> true),
        LogAuction("Auction Items", (NotUsed) -> Config.LOG_AUCTION),
        LogAuctionHistory("Auction History", (NotUsed) -> Config.LOG_AUCTION),
        LogBlocked("Blocked/Banned History", (NotUsed) -> true),
        LogCharacter("Character List", (NotUsed) -> Config.LOG_CHARACTER_LIST),
        LogAccountFull("Login History (Full)", (NotUsed) -> Config.LOG_FULL_LOG_ACCOUNT),
        LogAccountFlattened("Login History (Flattened)", (NotUsed) -> true),
        LogDonate("Purchase History", (NotUsed) -> Config.LOG_DONATE),
        LogInventory("Items (all characters)", (NotUsed) -> Config.LOG_INVENTORY),
        LogItemBagData("Item Bag Data (all characters)", (NotUsed) -> Config.LOG_INVENTORY),
        LogEquips("Equips (all characters)", (NotUsed) -> Config.LOG_EQUIPS),
        LogLocker("Locker Items (all characters)", (NotUsed) -> Config.LOG_LOCKER),
        LogLockerItemInfo("Locker Item Info (all characters)", (NotUsed) -> Config.LOG_LOCKER),
        LogLockerEquips("Locker Equips (all characters)", (NotUsed) -> Config.LOG_LOCKER && Config.LOG_EQUIPS),
        LogTrunk("Trunk (Money)", (NotUsed) -> Config.LOG_LOCKER),
        LogTrunkItems("Trunk Items (all characters)", (NotUsed) -> Config.LOG_LOCKER),
        LogTrunkEquips("Trunk Equips (all characters)", (NotUsed) -> Config.LOG_LOCKER && Config.LOG_EQUIPS),
        LogSuspectedAccounts("Suspected Shared Accounts", (NotUsed) -> Config.LOG_SUSPECTED_ACCOUNTS);

        private String sTitle;
        private Predicate<Void> bIsLogged;

        ListTypes(String sTitle, Predicate<Void> bIsLogged) {
            this.sTitle = sTitle;
            this.bIsLogged = bIsLogged;
        }

        public String GetTitle() {
            return this.sTitle;
        }

        public boolean IsVisible() {
            return bIsLogged.test(null);
        }
    }
}
