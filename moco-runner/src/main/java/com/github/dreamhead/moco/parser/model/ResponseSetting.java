package com.github.dreamhead.moco.parser.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dreamhead.moco.ResponseHandler;
import com.github.dreamhead.moco.handler.AndResponseHandler;
import com.github.dreamhead.moco.resource.Resource;
import com.google.common.base.Function;
import com.google.common.base.Objects;

import java.util.List;
import java.util.Map;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.handler.ResponseHandlers.responseHandler;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ResponseSetting {
    private String status;
    private ProxyContainer proxy;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private Long latency;
    private TextContainer text;
    private TextContainer file;
    @JsonProperty("path_resource")
    private String pathResource;
    private String version;

    public Resource retrieveResource() {
        if (text != null) {
            if (text.isRawText()) {
                return text(text.getText());
            }

            if ("template".equalsIgnoreCase(text.getOperation())) {
                return template(text.getText());
            }
        }

        if (file != null) {
            if (file.isRawText()) {
                return file(file.getText());
            }

            if ("template".equalsIgnoreCase(file.getOperation())) {
                return template(file(file.getText()));
            }
        }

        if (pathResource != null) {
            return pathResource(pathResource);
        }

        if (version != null) {
            return version(version);
        }

        throw new IllegalArgumentException("unknown response setting with " + this);
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .add("text", text)
                .add("file", file)
                .add("version", version)
                .add("status", status)
                .add("headers", headers)
                .add("cookies", cookies)
                .add("proxy", proxy)
                .add("latency", latency)
                .toString();
    }

    public boolean isResource() {
        return (text != null)
                || (file != null)
                || (pathResource != null)
                || (version != null);
    }

    public ResponseHandler getResponseHandler() {
        List<ResponseHandler> handlers = newArrayList();
        if (isResource()) {
            handlers.add(responseHandler(retrieveResource()));
        }

        if (status != null) {
            handlers.add(status(Integer.parseInt(status)));
        }

        if (headers != null) {
            handlers.add(toResponseHandler(headers, toHeaderResponseHandler()));
        }

        if (latency != null) {
            handlers.add(latency(latency));
        }

        if (cookies != null) {
            handlers.add(toResponseHandler(cookies, toCookieResponseHandler()));
        }

        if (proxy != null) {
            handlers.add(createProxy(proxy));
        }

        if (handlers.isEmpty()) {
            throw new IllegalArgumentException("unknown response setting with " + this);
        }

        return handlers.size() == 1 ? handlers.get(0) : new AndResponseHandler(handlers);
    }

    private ResponseHandler createProxy(ProxyContainer proxy) {
        if (proxy.getFailover() != null) {
            return proxy(proxy.getUrl(), failover(proxy.getFailover()));
        }

        return proxy(proxy.getUrl());
    }

    private AndResponseHandler toResponseHandler(Map<String, String> map,
                                                 Function<Map.Entry<String, String>, ResponseHandler> function) {
        return new AndResponseHandler(from(map.entrySet()).transform(function));
    }

    private Function<Map.Entry<String, String>, ResponseHandler> toHeaderResponseHandler() {
        return new Function<Map.Entry<String, String>, ResponseHandler>() {
            @Override
            public ResponseHandler apply(Map.Entry<String, String> entry) {
                return header(entry.getKey(), entry.getValue());
            }
        };
    }

    private Function<Map.Entry<String, String>, ResponseHandler> toCookieResponseHandler() {
        return new Function<Map.Entry<String, String>, ResponseHandler>() {
            @Override
            public ResponseHandler apply(Map.Entry<String, String> entry) {
                return cookie(entry.getKey(), entry.getValue());
            }
        };
    }
}
