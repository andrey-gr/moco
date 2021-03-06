package com.github.dreamhead.moco.internal;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class MocoHttpServer {
    private final MocoServer server = new MocoServer();
    private final ActualHttpServer serverSetting;

    public MocoHttpServer(ActualHttpServer serverSetting) {
        this.serverSetting = serverSetting;
    }

    public void start() {
        server.start(serverSetting.getPort(), new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new MocoHandler(serverSetting));
                return pipeline;
            }
        });
    }

    public void stop() {
        server.stop();
    }
}
