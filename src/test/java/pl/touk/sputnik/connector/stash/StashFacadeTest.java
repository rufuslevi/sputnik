package pl.touk.sputnik.connector.stash;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pl.touk.sputnik.configuration.ConfigurationSetup;
import pl.touk.sputnik.connector.FacadeConfigUtil;
import pl.touk.sputnik.review.ReviewFile;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class StashFacadeTest {

    private static String SOME_PULL_REQUEST_ID = "12314";
    private static String SOME_REPOSITORY = "repo";
    private static String SOME_PROJECT_KEY = "key";

    private static final Map<String, String> STASH_PATCHSET_MAP = ImmutableMap.of(
            "cli.pullRequestId", SOME_PULL_REQUEST_ID,
            "connector.repositorySlug", SOME_REPOSITORY,
            "connector.projectKey", SOME_PROJECT_KEY
    );

    private StashFacade fixture;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(FacadeConfigUtil.HTTP_PORT);

    @Before
    public void setUp() {
        new ConfigurationSetup().setUp(FacadeConfigUtil.getHttpConfig("stash"), STASH_PATCHSET_MAP);
        fixture = new StashFacadeBuilder().build();
    }

    @Test
    public void shouldGetChangeInfo() throws Exception {
        stubFor(get(urlEqualTo(String.format(
                    "/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/changes",
                    SOME_PROJECT_KEY, SOME_REPOSITORY, SOME_PULL_REQUEST_ID)))
                .withHeader("Authorization", equalTo("Basic dXNlcjpwYXNz"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IOUtils.toString(getClass().getResourceAsStream("/json/stash-changes.json")))));

        List<ReviewFile> files = fixture.listFiles();
        assertThat(files).hasSize(4);
    }

    @Test
    public void shouldReturnDiffAsMapOfLines() throws Exception {
        stubFor(get(urlMatching(String.format(
                "/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/diff.*",
                SOME_PROJECT_KEY, SOME_REPOSITORY, SOME_PULL_REQUEST_ID)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IOUtils.toString(getClass().getResourceAsStream("/json/stash-diff.json")))));

        SingleFileChanges singleFileChanges = fixture.changesForSingleFile("src/main/java/Main.java");
        assertThat(singleFileChanges.getFilename()).isEqualTo("src/main/java/Main.java");
        assertThat(singleFileChanges.getChangesMap())
                .containsEntry(1, ChangeType.ADDED)
                .containsEntry(2, ChangeType.ADDED);
    }
}
