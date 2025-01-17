package pl.touk.sputnik.configuration;

import lombok.Getter;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;

public class CliWrapper {

    @Getter
    private final Options options;

    public CliWrapper() {
        options = createOptions();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private Options createOptions() {
        Options localOptions = new Options();
        localOptions.addOption(buildOption(CliOption.CONF, true, true));

        localOptions.addOption(buildOption(CliOption.CHANGE_ID, true, false));
        localOptions.addOption(buildOption(CliOption.REVISION_ID, true, false));
        localOptions.addOption(buildOption(CliOption.VERSION, false, false));

        localOptions.addOption(buildOption(CliOption.PULL_REQUEST_ID, true, false));
        localOptions.addOption(buildOption(CliOption.API_KEY, true, false));
        localOptions.addOption(buildOption(CliOption.BUILD_ID, true, false));
        localOptions.addOption(buildOption(CliOption.PROVIDER, true, false));
        localOptions.addOption(buildOption(CliOption.FILE_REGEX, true, false));
        localOptions.addOption(buildOption(CliOption.USERNAME, true, false));
        localOptions.addOption(buildOption(CliOption.PASSWORD, true, false));

        return localOptions;
    }

    public CommandLine parse(@NotNull String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        CommandLine parsedOptions = null;
        try {
            parsedOptions = parser.parse(options, args);
        } catch (ParseException e) {
            boolean found = false;
            for (String elem : args) {
                if (elem.equals("--version")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw e;
            }
        }

        return parsedOptions;
    }

    @NotNull
    @SuppressWarnings("all")
    private Option buildOption(@NotNull CliOption name, boolean hasArgs, boolean isRequired) {
        return Option.builder().argName(name.getCommandLineParam())
            .longOpt(name.getCommandLineParam())
            .hasArg(hasArgs)
            .required(isRequired)
            .desc(name.getDescription())
            .build();
    }
}
