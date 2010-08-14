/**
 * @(#) NoPackageTest.java;
 * <p/>
 * Created on Aug 13, 2010
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 * /**
 */


import net.codemate.DbAssert;
import net.codemate.DbSource;
import net.codemate.Fixture;
import org.junit.Test;

public class NoPackageTest {


    @Test
    public void testFixtureLoadingOutsidePackage() {
        DbAssert dbAssert = DbAssert.init("net/codemate/databases.yml");
        DbSource testSource = dbAssert.source("testSource", getClass());
        testSource.clean_table("events");
        final Fixture eventsFixture = testSource.fixture("events");

    }


}
