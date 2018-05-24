package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.Nonnull;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ulli Hafner
 */
public class ToolConfiguration extends AbstractDescribableImpl<ToolConfiguration> {
    private final String pattern;
    private final StaticAnalysisTool tool;

    /**
     * Creates a new instance of {@link ToolConfiguration}.
     *
     * @param pattern
     *         the pattern of files to parse
     * @param tool
     *         the name of the parser to use
     */
    @DataBoundConstructor
    public ToolConfiguration(final String pattern, final StaticAnalysisTool tool) {
        super();

        this.pattern = pattern;
        this.tool = tool;
    }

    /**
     * Returns the static analysis tool that will scan files and create issues.
     *
     * @return the tool
     */
    public StaticAnalysisTool getTool() {
        return tool;
    }

    /**
     * Returns the Ant file-set pattern of files to work with. If the pattern is undefined then the console log is
     * scanned.
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", tool.getName(), pattern);
    }

    /**
     * Dummy descriptor for {@link ToolConfiguration}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ToolConfiguration> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }

        /**
         * Performs on-the-fly validation on the ant pattern for input files.
         *
         * @param project
         *         the project
         * @param pattern
         *         the file pattern
         *
         * @return the validation result
         */
        public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
                @QueryParameter final String pattern) {
            if (project != null) { // there is no workspace in pipelines
                try {
                    FilePath workspace = project.getSomeWorkspace();
                    if (workspace != null && workspace.exists()) {
                        String result = workspace.validateAntFileMask(pattern,
                                FilePath.VALIDATE_ANT_FILE_MASK_BOUND);
                        if (result != null) {
                            return FormValidation.error(result);
                        }
                    }
                }
                catch (InterruptedException | IOException ignore) {
                    // ignore and return ok
                }
            }

            return FormValidation.ok();
        }
    }
}

