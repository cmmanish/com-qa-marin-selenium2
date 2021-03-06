package com.marin.qa.selenium.campaigns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.junit.*;
import org.openqa.selenium.WebDriver;

import com.marin.qa.selenium.WebdriverBaseClass;
import com.marin.qa.selenium.common.MarinApp;
import com.marin.qa.selenium.Util.QaRandom;
import com.marin.qa.selenium.pageObjects.pages.ActivityLogPage;
import com.marin.qa.selenium.pageObjects.pages.BulkAddEditCampaignsPage;
import com.marin.qa.selenium.pageObjects.pages.CampaignSettingsPage;
import com.marin.qa.selenium.pageObjects.pages.CampaignsPage;
import com.marin.qa.selenium.pageObjects.pages.HomePage;
import com.marin.qa.selenium.pageObjects.pages.NewGoogleCampaignPage.CampaignStatus;
import com.marin.qa.selenium.pageObjects.pages.SingleCampaignPage;

public class BulkAddCampaignsTest extends WebdriverBaseClass {

    
    public static Logger log = Logger.getLogger(BulkAddCampaignsTest.class);

    @Rule
    public static WebDriver driver = MarinApp.getApp();
    public QaRandom random = QaRandom.getInstance();
    
    public BulkAddCampaignsTest (){
        log.info("Now Running BulkAddCampaignsTest Suite");  
    }
    
    @BeforeClass
    public static void testSetUp() {
        log.info("<--------- Start Setup Test --------->");
        loginSuccessful(driver);
        clearAllPendingChanges(driver);
        log.info("<--------- End Setup Test --------->");
    }

    @AfterClass
    public static void testCleanup() {
        log.info("<--------- Start Logout Test --------->");
        clearAllPendingChanges(driver);
        HomePage homePage = HomePage.getInstance();
        homePage.click(driver, HomePage.Link.Admin);
        homePage.click(driver, HomePage.Link.Logout);
        driver.close();
        log.info("<--------- End Logout Test --------->");
    }

    @After
    public void RunAfterEachTest() {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        log.info("Running RunAfterEachTest()");
        HomePage homePage = HomePage.getInstance();
        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public void verifyAndPostCartop(String description) {

        log.info("go to activity log and verify the Cartops");
        HomePage homePage = HomePage.getInstance();
        homePage.click(driver, HomePage.Link.Admin);
        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();
        String postCount = activityLogPage.getInfo(driver, ActivityLogPage.Label.PostCount);
        int tryCount = 0;

        while ("0".equalsIgnoreCase(postCount) && tryCount != 5) {
            homePage.click(driver, HomePage.Link.Admin);
            postCount = activityLogPage.getInfo(driver, ActivityLogPage.Label.PostCount);
            tryCount ++;
        }

        String cartop = activityLogPage.getInfo(driver, ActivityLogPage.Column.ID, ActivityLogPage.Column.Description, description);
        if ("".equalsIgnoreCase(cartop)) {
            homePage.click(driver, HomePage.Link.Admin);
            cartop = activityLogPage.getInfo(driver, ActivityLogPage.Column.ID, ActivityLogPage.Column.Description, description);
        }
        assertNotNull("Can't find the cartop. Something is fishy", cartop);
        log.info("cartop is " + cartop);
        activityLogPage.check(driver, ActivityLogPage.Column.ID, cartop);
        activityLogPage.click(driver, ActivityLogPage.Button.PostNow);

        try {
            assertEquals("Cartop failed ", "Succeeded", activityLogPage.waitForCartopStatus(driver, cartop));
        }
        catch (AssertionError e) {
            e.toString();
        }
    }
    /*
     * Sample Bulk Sheet
     * Account Campaign Network Merchant ID Country of Sale Campaign Priority Shopping Channels
     * Goog301 active shop app Shopping 100543509 US Low Online
     */
    @Test
    public void T1BulkCreateGoogleShoppingCampaignUSOnline() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String account = GOOGLE_ACCOUNT;
        String campaignName = random.getRandomStringWithPrefix("CampaignName", 5);
        String status = "Active";
        String dailyBudget = "1." + random.getRandomInteger(2);
        String network = "Shopping";
        String merchantId = "100543509";
        String countryOfSale = "United States";
        String campaignPriority = "High";
        String shoppingChannels = "online";
        String bulkCreateCampaign = "Bulk Create: Google Campaign: " + campaignName + ".";

        calendar.setTime(Calendar.getInstance().getTime());
        final String startDate = groupFormaterDate.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        final String endDate = groupFormaterDate.format(calendar.getTime());

        String headers = "Account\tCampaign\tStatus\tStart Date\tCampaign End Date\tDaily Budget\tNetwork\tMerchant ID\tCountry of Sale\tCampaign Priority\tShopping Channels\\n";

        String contents = account + TAB + campaignName + TAB + status + TAB + startDate + TAB + endDate + TAB + dailyBudget + TAB + network + TAB + merchantId + TAB + countryOfSale + TAB
                + campaignPriority + TAB + shoppingChannels + END_OF_LINE;

        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();

        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkCreateCampaign);
        
        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", CampaignStatus.ACTIVE.toString(), campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));
        assertEquals("Campaign Merchant Id in the Settings Page don't match ", merchantId, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.MerchantId));
        assertEquals("Campaign budget in the Settings Page don't match ", countryOfSale, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.CountryOfSale));

        assertEquals("Campaign budget in the Settings Page don't match ", dailyBudget, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget));

        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Test
    public void T2BulkCreateGoogleShoppingCampaignUSShoppingChannelBoth() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String account = GOOGLE_ACCOUNT;
        String campaignName = random.getRandomStringWithPrefix("CampaignName", 5);
        String status = "Active";
        String dailyBudget = "1." + random.getRandomInteger(2);
        String network = "Shopping";
        String merchantId = "100543509";
        String countryOfSale = "United States";
        String campaignPriority = "High";
        String shoppingChannels = "online,local";

        String bulkCreateCampaign = "Bulk Create: Google Campaign: " + campaignName + ".";

        calendar.setTime(Calendar.getInstance().getTime());
        final String startDate = groupFormaterDate.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        final String endDate = groupFormaterDate.format(calendar.getTime());

        String headers = "Account\tCampaign\tStatus\tStart Date\tCampaign End Date\tDaily Budget\t" + "Network\tMerchant ID\tCountry of Sale\tCampaign Priority\tShopping Channels\\n";

        String contents = account + TAB + campaignName + TAB + status + TAB + startDate + TAB + endDate + TAB + dailyBudget + TAB + network + TAB + merchantId + TAB + countryOfSale + TAB
                + campaignPriority + TAB + shoppingChannels+ TAB + END_OF_LINE;

        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();

        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.select(driver,BulkAddEditCampaignsPage.DropDownMenu.PublisherAccount,"Select");
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkCreateCampaign);

        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", CampaignStatus.ACTIVE.toString(), campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));
        assertEquals("Campaign budget in the Settings Page don't match ", dailyBudget, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget));

        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    /*
     * Sample Bulk Sheet
     * Account Campaign Network Merchant ID Country of Sale Campaign Priority Shopping Channels
     * Goog301 active shop app Shopping 100543509 US Low Online
     */
    @Test
    public void T3BulkCreateGoogleDispalyCampaign() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String account = GOOGLE_ACCOUNT;
        String campaignName = random.getRandomStringWithPrefix("CampaignName", 5);
        String status = "Active";
        String dailyBudget = "1." + random.getRandomInteger(2);
        String network = "display select";
        String bulkCreateCampaign = "Bulk Create: Google Campaign: " + campaignName + ".";

        calendar.setTime(Calendar.getInstance().getTime());
        final String startDate = groupFormaterDate.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        final String endDate = groupFormaterDate.format(calendar.getTime());

        String headers = "Account\tCampaign\tStatus\tStart Date\tCampaign End Date\tDaily Budget\tNetwork\\n";

        String contents = account + TAB + campaignName + TAB + status + TAB + startDate + TAB + endDate + TAB + dailyBudget + TAB + network+ END_OF_LINE;

        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();

        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.select(driver,BulkAddEditCampaignsPage.DropDownMenu.PublisherAccount,"Select");
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkCreateCampaign);

        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", CampaignStatus.ACTIVE.toString(), campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));

        assertEquals("Campaign budget in the Settings Page don't match ", dailyBudget, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget));
        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

    }
    /*

     */
    @Test
    public void T4BulkEditPriorityGoogleShoppingCampaignUS() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String campaignName = "auto_ShoppingCampaign_Priority";

        log.info("Get the current Priority ahead of time");
        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();
        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();

        // get the Campaign Setting value ahead of time
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab
        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);
        String oldCampaignPriority = campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.CampaignPriority);

        //Variables needed for the tests
        log.info("Variables needed for the tests ");

        String campaignPriority = random.getRandomElementWithException(CampaignPriority,oldCampaignPriority);
        String account = GOOGLE_ACCOUNT;
        String status = CampaignStatus.ACTIVE.toString();
        String merchantId = "100543509";
        String countryOfSale = "United States";
        String bulkEditCampaign = "Bulk Edit: Google Campaign: " + campaignName + ".";
        final String startDate = "8/22/14";
        final String endDate = "12/31/14";

        String headers = "Account\tCampaign\tCampaign Priority\\n";
        String contents = account + TAB + campaignName + TAB + campaignPriority + END_OF_LINE;

        homePage.click(driver, HomePage.Link.Admin);
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.select(driver,BulkAddEditCampaignsPage.DropDownMenu.PublisherAccount,"Select");
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkEditCampaign);

        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", status, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));
        assertEquals("Campaign Budget in the Settings Page don't match ", merchantId, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.MerchantId));
        assertEquals("Campaign CountryOfSale in the Settings Page don't match ", countryOfSale, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.CountryOfSale));
        assertEquals("Campaign Priority in the Settings Page don't match ", campaignPriority, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.CampaignPriority));

        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Test
    public void T5BulkEditShoppingChannelCampaignUS() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String account = GOOGLE_ACCOUNT;
        String campaignName = "auto_ShoppingCampaign_Channel";
        
        log.info("Get the current Shoppping Channel ahead of time");
        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();
        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();

        // get the Campaign Setting value ahead of time
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab
        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);
        
        //get the Shoppping Channel checkbox values
        String campaignPriority = campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.CampaignPriority);
        String dailyBudget = campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget);
        
        String countryOfSale = "United States";
        String status = CampaignStatus.ACTIVE.toString();
        String merchantId = "100543509";
        String bulkEditCampaign = "Bulk Edit: Google Campaign: " + campaignName + ".";
        final String startDate = "9/15/14";
        final String endDate = "12/31/14";
        
        String shoppingChannel = "";

        String headers = "Account\tCampaign\tShopping Channels\\n";
        String contents = account + TAB + campaignName + TAB + shoppingChannel + END_OF_LINE;

        homePage.click(driver, HomePage.Link.Admin);
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.select(driver,BulkAddEditCampaignsPage.DropDownMenu.PublisherAccount,"Select");
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkEditCampaign);

        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", status, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));
        assertEquals("Campaign Merchant ID in the Settings Page don't match ", merchantId, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.MerchantId));
        assertEquals("Campaign budget in the Settings Page don't match ", countryOfSale, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.CountryOfSale));
        assertEquals("Campaign Priority in the Settings Page don't match ", campaignPriority, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.CampaignPriority));
        assertEquals("Campaign budget in the Settings Page don't match ", dailyBudget, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget));

        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Test
    public void T6BulkEditBudgetGoogleShoppingCampaignUS() throws Exception {

        log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String account = GOOGLE_ACCOUNT;
        String campaignName = "auto_ShoppingCampaign_budget";
        String status = CampaignStatus.ACTIVE.toString();
        String dailyBudget = "1." + random.getRandomInteger(2);
        String merchantId = "100543509";
        String campaignPriority = random.getRandomElement(CampaignPriority);

        String bulkEditCampaign = "Bulk Edit: Google Campaign: " + campaignName + ".";

        final String startDate = "9/12/14";
        final String endDate = "12/31/14";

        String headers = "Account\tCampaign\tDaily Budget\\n";
        String contents = account + TAB + campaignName + TAB + dailyBudget + END_OF_LINE;

        HomePage homePage = HomePage.getInstance();
        CampaignsPage campaignsPage = CampaignsPage.getInstance();

        ActivityLogPage activityLogPage = ActivityLogPage.getInstance();
        activityLogPage.click(driver, ActivityLogPage.Link.Campaigns);
        BulkAddEditCampaignsPage bulkAddEditCampaignsPage = BulkAddEditCampaignsPage.getInstance();
        bulkAddEditCampaignsPage.select(driver,BulkAddEditCampaignsPage.DropDownMenu.PublisherAccount,"Select");
        bulkAddEditCampaignsPage.type(driver, BulkAddEditCampaignsPage.TextArea.Campaigns, headers + contents);
        bulkAddEditCampaignsPage.click(driver, BulkAddEditCampaignsPage.Button.Process);

        verifyAndPostCartop(bulkEditCampaign);
        // Verify the Campaign Settings
        homePage.select(driver, HomePage.Tab.Campaigns);
        campaignsPage.select(driver, CampaignsPage.DropDownMenu.Views, CampaignsPage.CAMPAIGN_VIEW);

        // Open the Campaign via clicking the link and go to Settings tab

        assertEquals("Campaign " + campaignName + " couldn't be opened ", true, campaignsPage.open(driver, campaignName));

        SingleCampaignPage singleCampaignPage = SingleCampaignPage.getInstance();
        singleCampaignPage.select(driver, SingleCampaignPage.Tab.Settings);

        CampaignSettingsPage campaignSettingsPage = CampaignSettingsPage.getInstance();
        assertEquals("Campaign Name in the Settings Page don't match ", campaignName, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.CampaignName));
        assertEquals("Campaign Status in the Settings Page don't match ", status, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.Status));
        assertEquals("Campaign Start Date in the Settings Page don't match ", startDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.StateDate));
        assertEquals("Campaign End Date in the Settings Page don't match ", endDate, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.EndDate));
        assertEquals("Campaign budget in the Settings Page don't match ", merchantId, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.MerchantId));
        // assertEquals("Campaign budget in the Settings Page don't match ", countryOfSale, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.Label.CountryOfSale));
        assertEquals("Campaign Priority in the Settings Page don't match ", campaignPriority, campaignSettingsPage.getSelected(driver, CampaignSettingsPage.DropDownMenu.CampaignPriority));

        assertEquals("Campaign budget in the Settings Page don't match ", dailyBudget, campaignSettingsPage.getInfo(driver, CampaignSettingsPage.TextInput.Budget));

        homePage.click(driver, HomePage.Link.Admin);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}
