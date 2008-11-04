/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package org.jboss.seam.example.dvd.test.selenium;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

/**
 * This class tests search functionality of the example
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class SearchTest extends SeleniumDvdTest {

    /**
     * This test does simple search for dvd.
     */
    @Test(groups = { "search" }, dependsOnGroups = { "login.basic" })
    public void testSearch() {
        String searchString = "top gun";
        browser.click(getProperty("SHOP"));
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("Navigation failed.", browser.getLocation().contains(
                getProperty("SHOP_URL")));
        browser.type(getProperty("SEARCH_FIELD"), searchString);
        browser.click(getProperty("SEARCH_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        assertEquals("Unexpected number of results. One result expected.", 1,
                browser.getXpathCount(getProperty("SEARCH_RESULT_ITEM")));
        browser.click(getProperty("SEARCH_RESULT_FIRST_ROW_LINK"));
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("Navigation failure.", browser.getLocation().contains(
                getProperty("DVD_URL")));
    }

    /**
     * This test does simple search in two windows verifying they do not affect
     * each other
     */
    @Test(dependsOnMethods = { "testSearch" }, dependsOnGroups = { "login.basic" })
    public void testMultipleWindowSearch() {
        String searchString1 = "Forrest Gump";
        String searchString2 = "The Shawshank Redemption";

        // browser.openWindow(getProperty("HOME_PAGE"), "1");
        browser.openWindow(getProperty("HOME_PAGE"), "1");
        browser.selectWindow("1");
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("User should be logged in by now.", isLoggedIn(browser));
        // if (!isLoggedIn(browser)) {
        // login(browser, USERNAME, PASSWORD);
        // }
        // search for dvd in first window
        browser.openWindow(getProperty("HOME_PAGE"), "2");
        browser.selectWindow("2");
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("User should be logged in by now.", isLoggedIn(browser));
        // if (!isLoggedIn(browser)) {
        // login(browser, USERNAME, PASSWORD);
        // }
        browser.click(getProperty("SHOP"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("SEARCH_FIELD"), searchString1);
        browser.click(getProperty("SEARCH_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        assertEquals("Unexpected search result in first window.",
                searchString1, browser
                        .getText(getProperty("SEARCH_RESULT_FIRST_ROW_LINK")));
        // search for dvd in second window
        browser.selectWindow("1");
        browser.click(getProperty("SHOP"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("SEARCH_FIELD"), searchString2);
        browser.click(getProperty("SEARCH_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        assertEquals("Unexpected search result in second window.",
                searchString2, browser
                        .getText(getProperty("SEARCH_RESULT_FIRST_ROW_LINK")));
        browser.selectWindow("2");
        browser.refresh();
        browser.waitForPageToLoad(TIMEOUT);
        assertEquals("Unexpected search result in first window after refresh.",
                searchString1, browser
                        .getText(getProperty("SEARCH_RESULT_FIRST_ROW_LINK")));

    }
}