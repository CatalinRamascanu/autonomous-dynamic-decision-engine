package com.adobe.primetime.adde;

import com.adobe.primetime.adde.fetcher.FetcherParser;

public class EsmJsonParser implements FetcherParser {
    @Override
    public String parseInputJson(String inputJson) {
        String testJson = "[{\"authn-pending\":\"143099581\",\"clientless-tokens\":\"7386414\",\"authz-failed\":\"31258558\",\"media-tokens\":\"876824545\",\"authz-rejected\":\"4346589\",\"authz-latency\":\"345347096759\",\"authn-successful\":\"124419316\",\"authn-attempts\":\"199855724\",\"year\":\"2015\",\"authz-successful\":\"377597150\",\"clientless-failures\":\"184851374\",\"authz-attempts\":\"418177654\"}]";
        return testJson;
    }
}
