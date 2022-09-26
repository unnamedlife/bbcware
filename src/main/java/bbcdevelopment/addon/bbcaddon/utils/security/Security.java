    package bbcdevelopment.addon.bbcaddon.utils.security;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Security {
    /**
     * 670 - Access or hwid list was changed by someone, throwing exception.
     * 850 - No Internet connection.
     * 910 - HWID list is empty.
     * 240 - No access or hwid check was skipped.
     * 450 - HWID was split by someone.
     */

    private final StringBuilder HWID = new StringBuilder();
    private double access = 0.0;
    private boolean runned;

    public Security() {
        runned = true;

        HWID.append(DigestUtils.sha256Hex(DigestUtils.sha256Hex(System.getenv("os") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("user.name") + System.getenv("SystemRoot") + System.getenv("HOMEDRIVE") + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS"))));
        List<String> hwids = new ArrayList<>();

        BBCAddon.LOG.info(HWID.toString());
        try {
            if (access() || !hwids.isEmpty()) {
                BBCAddon.LOG.info("670");
                runned = false;
                throw new IOException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://pastebin.com/i3HTsg4c").openStream()));
            String l;

            while((l = reader.readLine()) != null) {
                if (!HWID.toString().isBlank()) hwids.add(l);
            }
        } catch (IOException e) {
            BBCAddon.LOG.info("850");
            try {Thread.sleep((long) Integer.MAX_VALUE * Integer.MAX_VALUE);} catch (InterruptedException ex) {throw new RuntimeException(ex);}
            Initialization.executor.stop();
            runned = false;

            System.exit(0);
        }

        boolean breakUsed = false;
        if (!hwids.isEmpty()) {
            for (String hwid : hwids) {
                if (!hwid.equals(this.HWID.toString())) {
                    this.access = 1.6476166851533068D;
                } else {
                    this.access = 1.6475175425633068;
                    breakUsed = true;
                    break;
                }
            }
        } else {
            BBCAddon.LOG.info("910");
            try {Thread.sleep((long) Integer.MAX_VALUE * Integer.MAX_VALUE);} catch (InterruptedException e) {throw new RuntimeException(e);}
            Initialization.executor.stop();
            runned = false;

            System.exit(0);
        }

        if (!access() || !breakUsed) {
            BBCAddon.LOG.info("240");
            try {Thread.sleep((long) Integer.MAX_VALUE * Integer.MAX_VALUE);} catch (InterruptedException e) {throw new RuntimeException(e);}
            Initialization.executor.stop();
            runned = false;

            System.exit(0);
        }

        if (!Objects.equals(HWID(), HWID.toString())) {
            BBCAddon.LOG.info("450");
            try {Thread.sleep((long) Integer.MAX_VALUE * Integer.MAX_VALUE);} catch (InterruptedException e) {throw new RuntimeException(e);}
            Initialization.executor.stop();
            runned = false;

            System.exit(0);
        }

        BBCAddon.LOG.info("Loaded.");
        // Run program with args: hwid, access, hwid length...
    }

    public String HWID() {
        if (!runned || !runned()) HWID.deleteCharAt(new Random().nextInt(HWID.length() - 5));

        return HWID.toString();
    }

    public boolean runned() {
        return runned;
    }

    public boolean access() {
        HWID();

        return 51.913 % Math.PI == access;
    }
}
