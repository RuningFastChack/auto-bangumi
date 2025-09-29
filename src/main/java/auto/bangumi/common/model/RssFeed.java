package auto.bangumi.common.model;


import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "rss")
@XmlAccessorType(XmlAccessType.FIELD)
public class RssFeed {

    @XmlAttribute
    private String version;

    @XmlElement(name = "channel")
    private Channel channel;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Channel {
        private String title;
        private String link;
        private String description;

        @XmlElement(name = "item")
        private List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        private String guid;
        private String link;
        private String title;
        private String description;

        @XmlElement(name = "torrent", namespace = "https://mikanime.tv/0.1/")
        private Torrent torrent;

        @XmlElement(name = "enclosure")
        private Enclosure enclosure;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Torrent {
        private String link;
        private long contentLength;
        private String pubDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Enclosure {
        @XmlAttribute
        private String type;
        @XmlAttribute
        private long length;
        @XmlAttribute
        private String url;
    }
}