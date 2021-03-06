package com.scaleunlimited.flinkcrawler.urls;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.scaleunlimited.flinkcrawler.fetcher.MockUrlLengthenerFetcher;
import com.scaleunlimited.flinkcrawler.pojos.RawUrl;

public class SimpleUrlLengthenerTest {

    private static final String SHOULD_NOT_REDIRECT_HERE_URL = "http://should-not-redirect-here.com/";

    public static final Map<String, String> REDIRECTIONS = new HashMap<String, String>();

    private SimpleUrlLengthener _lengthener;
    private SimpleUrlNormalizer _normalizer = new SimpleUrlNormalizer();

    @Before
    public void setup() throws Throwable {
        REDIRECTIONS.clear();
        REDIRECTIONS.put(_normalizer.normalize("bit.ly/4526"),
                _normalizer.normalize("target-domain-1.com"));
        REDIRECTIONS.put(_normalizer.normalize("https://tinyurl.com/my-url-name"),
                _normalizer.normalize("target-domain-2.com"));
        REDIRECTIONS.put(_normalizer.normalize("tinyurl.com.my-domain.com/my-url-name"),
                SHOULD_NOT_REDIRECT_HERE_URL);
        MockUrlLengthenerFetcher fetcher = new MockUrlLengthenerFetcher(REDIRECTIONS);
        MockUrlLengthenerFetcher.MockUrlLengthenerFetcherBuilder fetcherBuilder = new MockUrlLengthenerFetcher.MockUrlLengthenerFetcherBuilder(
                fetcher);
        _lengthener = new SimpleUrlLengthener(fetcherBuilder);
        _lengthener.open();
    }

    @Test
    public void testLengthen() throws Throwable {
        for (Map.Entry<String, String> entry : REDIRECTIONS.entrySet()) {
            RawUrl sourceUrl = new RawUrl(entry.getKey());
            RawUrl targetUrl = _lengthener.lengthen(sourceUrl);
            if (entry.getValue().equals(SHOULD_NOT_REDIRECT_HERE_URL)) {
                Assert.assertEquals(sourceUrl.getUrl(), targetUrl.getUrl());
            } else {
                Assert.assertEquals(entry.getValue(), targetUrl.getUrl());
                Assert.assertEquals(targetUrl.getUrl(), _lengthener.lengthen(targetUrl).getUrl());
            }
        }
    }
    
    @Test
    public void testLruCache() throws Throwable {
        Map<Integer, Integer> testCache = SimpleUrlLengthener.makeLruCache(10);
        for (int i = 0; i < 10; i++) {
            testCache.put(i, i * 5);
        }
        for (int i = 0; i < 5; i++) {
            testCache.put(i * 2, i * 10);
        }
        for (int i = 0; i < 5; i++) {
            testCache.put((i+1) * 10, (i+1) * 10);
        }
        testCache.put(1000, 1000);
        for (int i = 0; i < 5; i++) {
            Assert.assertNull(testCache.get(0));
            if (i != 0) {
                Assert.assertEquals(i * 10, (int)(testCache.get(i * 2)));
            }
            Assert.assertEquals((i+1) * 10, (int)(testCache.get((i+1) * 10)));
            Assert.assertEquals(1000, (int)(testCache.get(1000)));
        }
    }
}
