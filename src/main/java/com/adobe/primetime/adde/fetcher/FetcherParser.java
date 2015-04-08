package com.adobe.primetime.adde.fetcher;

/**
 * The classes extending this interface should implement parseInputJson method.
 * The method will receive the fetched JSON from the URL in a String format and it should
 * return a new JSON in a string format. The new JSON should be in the following form:
 * [
 *      {
 *          "inputField1":"inputValue1",
 *          "inputField2":"inputValue2",
 *           ....
 *      },
 *      {
 *          "inputField1":"inputValue1",
 *          "inputField1":"inputValue1",
 *          ....
 *      }
 * ]
 */

public interface FetcherParser {
    String parseInputJson(String inputJson);
}
