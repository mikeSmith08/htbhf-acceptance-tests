package uk.gov.dhsc.htbhf.steps;

import lombok.extern.slf4j.Slf4j;
import uk.gov.dhsc.htbhf.page.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.dhsc.htbhf.page.PageName.*;
import static uk.gov.dhsc.htbhf.steps.ActionOptionsTestDataFactory.buildDefaultActionOptions;
import static uk.gov.dhsc.htbhf.steps.Constants.DOB_DAY;
import static uk.gov.dhsc.htbhf.steps.Constants.DOB_MONTH;
import static uk.gov.dhsc.htbhf.steps.Constants.DOB_YEAR;

/**
 * Contains common steps used by more than one step
 */
@Slf4j
public class CommonSteps extends BaseSteps {

    protected static ThreadLocal<ActionOptions> actionOptionsThreadLocal = new ThreadLocal<>();
    private Map<PageName, Consumer<ActionOptions>> pageActions;

    protected void enterDetailsUpToPage(PageName pageName) {
        performPageActions(pageName, buildDefaultActionOptions());
    }

    protected void enterDetailsUpToPage(PageName pageName, ActionOptions actionOptions) {
        performPageActions(pageName, actionOptions);
    }

    private void performPageActions(PageName pageName, ActionOptions actionOptions) {
        actionOptionsThreadLocal.set(actionOptions);
        GuidancePage applyPage = openApplyPage();
        applyPage.clickStartButton();
        for (Map.Entry<PageName, Consumer<ActionOptions>> entry : pageActions.entrySet()) {
            if (pageName == entry.getKey()) {
                break;
            }
            Consumer<ActionOptions> actionsForPage = entry.getValue();
            actionsForPage.accept(actionOptions);
        }
    }

    protected GuidancePage openApplyPage() {
        GuidancePage applyPage = getPages().getGuidancePageNoWait(PageName.APPLY);
        applyPage.open();
        return applyPage;
    }

    @PostConstruct
    private void buildDefaultStepPageActions() {
        pageActions = new LinkedHashMap<>();
        addActionToMapRespectingToggle(pageActions, SCOTLAND, (actionOptions) -> enterDoYouLiveInScotlandNoAndSubmit());
        addActionToMapRespectingToggle(pageActions, DATE_OF_BIRTH, (actionOptions) -> enterDateOfBirthAndSubmit());
        addActionToMapRespectingToggle(pageActions, DO_YOU_HAVE_CHILDREN, (actionOptions) -> enterDoYouHaveChildrenYesAndSubmit());
        addActionToMapRespectingToggle(pageActions, CHILD_DATE_OF_BIRTH, (actionOptions) -> enterOneChildsDateOfBirth());
        addActionToMapRespectingToggle(pageActions, ARE_YOU_PREGNANT, (actionOptions) -> {
            if (actionOptions.isClaimantPregnant()) {
                selectYesOnPregnancyPage();
            } else {
                selectNoOnPregnancyPage();
            }
        });
        addActionToMapRespectingToggle(pageActions, NAME, (actionOptions) -> enterName(actionOptions.getFirstName(), actionOptions.getLastName()));
        addActionToMapRespectingToggle(pageActions, NATIONAL_INSURANCE_NUMBER, (actionOptions) -> enterNino(actionOptions.getNino()));
        //TODO MRS 2019-09-04: Need to add postcode lookup here when required.
//        addActionToMapRespectingToggle(pageActions, POSTCODE, (actionOptions) -> {
//            setupPostcodeLookupWithResults(POSTCODE);
//            enterPostcode();
//        });
        addActionToMapRespectingToggle(pageActions, MANUAL_ADDRESS, (actionOptions) ->
                enterManualAddress(actionOptions.getAddressLine1(),
                        actionOptions.getAddressLine2(),
                        actionOptions.getTownOrCity(),
                        actionOptions.getCounty(),
                        actionOptions.getPostcode()));
        addActionToMapRespectingToggle(pageActions, PageName.PHONE_NUMBER, (actionOptions) -> enterPhoneNumber());
        addActionToMapRespectingToggle(pageActions, PageName.EMAIL_ADDRESS, (actionOptions) -> enterEmailAddress());
        addActionToMapRespectingToggle(pageActions, SEND_CODE, (actionOptions) -> selectTextOnSendCode());
        addActionToMapRespectingToggle(pageActions, ENTER_CODE, (actionOptions) -> enterConfirmationCodeAndSubmit());
        addActionToMapRespectingToggle(pageActions, CHECK_ANSWERS, (actionOptions) -> {
        });
    }

    private void addActionToMapRespectingToggle(Map<PageName, Consumer<ActionOptions>> pageActions, PageName pageName, Consumer<ActionOptions> pageAction) {
        if (toggleConfiguration.isPageEnabled(pageName)) {
            pageActions.put(pageName, pageAction);
        } else {
            log.info("Not adding page action for [{}] as it is toggled off", pageName);
        }
    }

    protected void enterConfirmationCodeAndSubmit() {
        EnterCodePage enterCodePage = getPages().getEnterCodePage();
        ConfirmationCodePage confirmationCodePage = new ConfirmationCodePage(getWebDriver(), sessionDetailsUrl);
        String confirmationCode = confirmationCodePage.getConfirmationCodeForSession();
        enterCodePage.open();
        enterCodePage.enterCode(confirmationCode);
        enterCodePage.clickContinue();
    }

    protected void selectTextOnSendCode() {
        SendCodePage sendCodePage = getPages().getSendCodePage();
        sendCodePage.selectText();
        sendCodePage.clickContinue();
    }

    protected void enterEmailAddress() {
        EmailAddressPage emailAddressPage = getPages().getEmailAddressPage();
        emailAddressPage.enterEmailAddress(Constants.EMAIL_ADDRESS);
        emailAddressPage.clickContinue();
    }

    protected void enterPhoneNumber() {
        PhoneNumberPage phoneNumberPage = getPages().getPhoneNumberPage();
        phoneNumberPage.enterPhoneNumber(Constants.PHONE_NUMBER);
        phoneNumberPage.clickContinue();
    }

    protected void enterManualAddress(String addressLine1, String addressLine2, String town, String county, String postcode) {
        ManualAddressPage manualAddressPage = getPages().getManualAddressPage();
        manualAddressPage.enterAddressLine1(addressLine1);
        manualAddressPage.enterAddressLine2(addressLine2);
        manualAddressPage.enterTownOrCity(town);
        manualAddressPage.enterCounty(county);
        manualAddressPage.enterPostcode(postcode);
        manualAddressPage.clickContinue();
    }

    protected void enterNino(String nino) {
        NationalInsuranceNumberPage nationalInsuranceNumberPage = getPages().getNationalInsuranceNumberPage();
        nationalInsuranceNumberPage.enterNino(nino);
        nationalInsuranceNumberPage.clickContinue();
    }

    protected void enterName(String firstName, String lastName) {
        NamePage namePage = getPages().getNamePage();
        namePage.enterName(firstName, lastName);
        namePage.clickContinue();
    }

    protected void selectYesOnPregnancyPage() {
        enterExpectedDeliveryDateOnPregnancyPage(2);
        getPages().getAreYouPregnantPage().clickContinue();
    }

    protected void enterExpectedDeliveryDateOnPregnancyPage(int monthIncrement) {
        AreYouPregnantPage areYouPregnantPage = getPages().getAreYouPregnantPage();
        areYouPregnantPage.selectYes();
        areYouPregnantPage.enterExpectedDeliveryDate(monthIncrement);
    }

    protected void selectNoOnPregnancyPage() {
        AreYouPregnantPage areYouPregnantPage = getPages().getAreYouPregnantPage();
        areYouPregnantPage.selectNo();
        areYouPregnantPage.clickContinue();
    }

    protected void enterOneChildsDateOfBirth() {
        ChildDateOfBirthPage childDateOfBirthPage = getPages().getChildDateOfBirthPage();
        childDateOfBirthPage.enterChild3OrUnderDetails(0);
        childDateOfBirthPage.clickContinue();
    }

    protected void enterDoYouHaveChildrenYesAndSubmit() {
        DoYouHaveChildrenPage doYouHaveChildrenPage = getPages().getDoYouHaveChildrenPage();
        doYouHaveChildrenPage.selectYesRadioButton();
        doYouHaveChildrenPage.clickContinue();
    }

    protected void enterDateOfBirthAndSubmit() {
        DateOfBirthPage dateOfBirthPage = getPages().getDateOfBirthPage();
        dateOfBirthPage.getDayInputField().enterValue(DOB_DAY);
        dateOfBirthPage.getMonthInputField().enterValue(DOB_MONTH);
        dateOfBirthPage.getYearInputField().enterValue(DOB_YEAR);
        dateOfBirthPage.clickContinue();
    }

    protected void enterDoYouLiveInScotlandNoAndSubmit() {
        ScotlandPage scotlandPage = getPages().getScotlandPage();
        scotlandPage.selectNoRadioButton();
        scotlandPage.clickContinue();
    }

    protected void acceptTermsAndConditionsAndSubmitApplication() {
        wireMockManager.setupWireMockMappingsWithStatus("ELIGIBLE");
        CheckAnswersPage checkAnswersPage = getPages().getCheckAnswersPage();
        checkAnswersPage.clickContinue();
        TermsAndConditionsPage termsAndConditionsPage = getPages().getTermsAndConditionsPage();
        termsAndConditionsPage.clickAcceptTermsAndConditionsCheckBox();
        termsAndConditionsPage.clickContinue();
    }

    protected void assertBackLinkPointsToPage(PageName expectedPage) {
        BasePage page = getPages().getPageByName(expectedPage);
        boolean backLinkPresent = page.isBackLinkPresent();
        assertThat(backLinkPresent)
                .as("Back link should be present on the page")
                .isTrue();

        String href = page.getBackLinkHref();
        String expectedUrl = page.getFullPath();
        boolean correctHref = isCorrectHref(href, expectedUrl);
        assertThat(correctHref)
                .as("back link url should be [" + expectedUrl + "], is [" + href + "]")
                .isTrue();
    }

    private boolean isCorrectHref(String href, String expectedUrl) {
        return (href.equals(expectedUrl) || href.startsWith(expectedUrl + "?"));
    }
}
