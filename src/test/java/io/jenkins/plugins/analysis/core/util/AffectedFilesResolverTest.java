package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

/**
 * Tests the class {@link AffectedFilesResolver}.
 *
 * @author Ullrich Hafner
 */
class AffectedFilesResolverTest {
    private static final FilePath BUILD_ROOT = new FilePath(new File("builds"));

    /** Ensures that illegal file names are processed without problems. */
    @ParameterizedTest(name = "[{index}] Illegal filename = {0}")
    @ValueSource(strings = {"/does/not/exist", "!<>$$&%/&(", "\0 Null-Byte"})
    void shouldReturnFallbackOnError(final String fileName) throws IOException, InterruptedException {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setFileName(fileName).build());
        new AffectedFilesResolver().copyFilesWithAnnotationsToBuildFolder(
                issues, mock(VirtualChannel.class), BUILD_ROOT);

        assertThat(issues.getErrorMessages()).hasSize(1);
        assertThat(issues.getErrorMessages().get(0)).startsWith("Copying 1 affected files to Jenkins' build folder builds.");
    }
}