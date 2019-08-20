package uk.gov.dhsc.htbhf.steps;

import io.cucumber.java.en.Then;
import uk.gov.dhsc.htbhf.page.CheckDetailsPage;
import uk.gov.dhsc.htbhf.page.CheckDetailsRowData;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains step definitions for the Check Your Details page.
 */
public class CheckDetailsSteps extends BaseSteps {

    private CheckDetailsPage checkDetailsPage;

    @Then("^I am shown the check details page with correct page content$")
    public void verifyCheckDetailsPageCorrect() {
        checkDetailsPage = new CheckDetailsPage(webDriver, baseUrl, webDriverWait);
        checkDetailsPage.waitForPageToLoad();
        assertThat(checkDetailsPage.getH1Text())
                .as("expected check page H1 text to not be empty")
                .isNotBlank();
        assertThat(checkDetailsPage.getH2Text())
                .as("expected check page H2 text to not be empty")
                .isNotBlank();
        assertThat(checkDetailsPage.getSubmitButtonText())
                .as("expected submit button text to not be empty")
                .isNotBlank();
        List<CheckDetailsRowData> claimContents = checkDetailsPage.getClaimSummaryListContents();
        assertHeaderAndChangeLinkShownForEachRow(claimContents);
        List<CheckDetailsRowData> childrenContents = checkDetailsPage.getChildrenSummaryListContents();
        assertHeaderTextShownForEachRow(childrenContents);
    }

    private void assertHeaderAndChangeLinkShownForEachRow(List<CheckDetailsRowData> claimContents) {
        List<CheckDetailsRowData> matchingRows = claimContents.stream()
                .filter(CheckDetailsRowData::hasChangeLinkAndHeaderText)
                .collect(Collectors.toList());
        assertThat(matchingRows)
                .as("Change link and header must be shown for each row in the claim summary")
                .hasSize(claimContents.size());
    }

    private void assertHeaderTextShownForEachRow(List<CheckDetailsRowData> childrenContents) {
        List<CheckDetailsRowData> matchingRows = childrenContents.stream()
                .filter(CheckDetailsRowData::hasHeaderText)
                .collect(Collectors.toList());
        assertThat(matchingRows)
                .as("Header text must be shown for each row in the children's date of birth summary")
                .hasSize(childrenContents.size());
    }

}