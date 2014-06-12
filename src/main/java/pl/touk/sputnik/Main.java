package pl.touk.sputnik;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;
import pl.touk.sputnik.configuration.CliWrapper;
import pl.touk.sputnik.configuration.Configuration;
import pl.touk.sputnik.connector.ConnectorFacade;
import pl.touk.sputnik.connector.ConnectorFacadeFactory;
import pl.touk.sputnik.review.Engine;

public final class Main {
    private static final String SPUTNIK = "sputnik";
    private static final String HEADER = "Sputnik - review your Gerrit patchset with Checkstyle, PMD and FindBugs";
    private static final int WIDTH = 120;

    private Main() {}

    public static void main(String[] args) {
        CliWrapper cliWrapper = new CliWrapper();
        CommandLine commandLine = null;
        try {
            commandLine = cliWrapper.parse(args);
        } catch (ParseException e) {
            printUsage(cliWrapper);
            System.out.println(e.getMessage());
            System.exit(1);
        }

        Configuration.instance().setConfigurationFilename(commandLine.getOptionValue(CliWrapper.CONF));
        Configuration.instance().init();
        Configuration.instance().updateWithCliOptions(commandLine);
        ConnectorFacade facade = ConnectorFacadeFactory.INSTANCE.build(Configuration.instance().getProperty("cli.connector"));

        new Engine(facade).run();
    }

    private static void printUsage(@NotNull CliWrapper cliOptions) {
        System.out.println(HEADER);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(WIDTH);
        helpFormatter.printHelp(SPUTNIK, cliOptions.getOptions(), true);
    }
}
