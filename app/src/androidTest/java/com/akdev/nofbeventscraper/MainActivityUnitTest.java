package com.akdev.nofbeventscraper;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityUnitTest {

    @Test
    public void TestSubdomainUrl() {


        Instrumentation mInstrumentation = getInstrumentation();
        // We register our interest in the activity
        Instrumentation.ActivityMonitor monitor = mInstrumentation.addMonitor(MainActivity.class.getName(), null, false);
        // We launch it
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(mInstrumentation.getTargetContext(), MainActivity.class.getName());
        mInstrumentation.startActivitySync(intent);

        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitor(monitor);

        // We register our interest in the next activity from the sequence in this use case
        mInstrumentation.removeMonitor(monitor);


        final String exp = "https://m.facebook.com/events/261145401687844";

        String url = "https://www.facebook.com/events/261145401687844";
        String act = mainActivity.checkURI(url);
        assertEquals(exp, act);

        url = "https://de-de.facebook.com/events/261145401687844";
        act = mainActivity.checkURI(url);
        assertEquals(exp, act);

        url = "https://m.facebook.com/events/261145401687844";
        act = mainActivity.checkURI(url);
        assertEquals(exp, act);

        url = "https://www.facebook.com/events/261145401687844/?active_tab=discussion";
        act = mainActivity.checkURI(url);
        assertEquals(exp, act);
    }

    @Test
    public void TestTimeToEpoch() {


        Instrumentation mInstrumentation = getInstrumentation();
        // We register our interest in the activity
        Instrumentation.ActivityMonitor monitor = mInstrumentation.addMonitor(MainActivity.class.getName(), null, false);
        // We launch it
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(mInstrumentation.getTargetContext(), MainActivity.class.getName());
        mInstrumentation.startActivitySync(intent);

        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitor(monitor);

        // We register our interest in the next activity from the sequence in this use case
        mInstrumentation.removeMonitor(monitor);


        String in = "2020-07-29T12:00:00+00:00";

        Long exp = new Long(1596024000);
        exp = exp* 1000;
        Long act = mainActivity.convertTimeToEpoch(in);
        assertEquals(exp, act);


        in = "2020-07-29T12:00:00+02:00";
        exp = new Long(1596016800);
        exp = exp* 1000;
        act = mainActivity.convertTimeToEpoch(in);
        assertEquals(exp, act);


        in = "1970-01-01T00:00:00+00:00";
        exp = new Long(0);
        exp = exp* 1000;
        act = mainActivity.convertTimeToEpoch(in);
        assertEquals(exp, act);

        in = "1970-01-01T02:00:00+02:00";
        exp = new Long(0);
        exp = exp* 1000;
        act = mainActivity.convertTimeToEpoch(in);
        assertEquals(exp, act);

    }

}
