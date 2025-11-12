package com.y11i.springcommddd.unit.posts.media;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.Url;
import com.y11i.springcommddd.posts.media.domain.exception.InvalidDisplayOrder;
import com.y11i.springcommddd.posts.media.domain.exception.InvalidMediaMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PostAssetTest {
//    @Test
//    @DisplayName("image() Factory: Creation successful")
//    void imageFactory_ok() {
//        var asset = PostAsset.image(
//                PostId.newId(),
//                0,
//                "https://cdn/app/img.jpg",
//                "https://cdn/app/img_t.jpg",
//                "image/jpeg",
//                800,
//                600,
//                "alt",
//                "cap"
//        );
//
//        assertThat(asset.mediaType()).isEqualTo(MediaType.IMAGE);
//        assertThat(asset.displayOrder()).isEqualTo(0);
//        assertThat(asset.srcUrl().value()).endsWith("img.jpg");
//        assertThat(asset.width()).isEqualTo(800);
//        assertThat(asset.height()).isEqualTo(600);
//        assertThat(asset.durationSec()).isNull();
//    }
//
//    @Test
//    @DisplayName("video() Factory: Creation Successful")
//    void videoFactory_ok() {
//        var asset = PostAsset.video(
//                PostId.newId(),
//                1,
//                "https://cdn/app/v.mp4",
//                "https://cdn/app/v_t.jpg",
//                "video/mp4",
//                1920,
//                1080,
//                3,
//                "alt",
//                "cap"
//        );
//
//        assertThat(asset.mediaType()).isEqualTo(MediaType.VIDEO);
//        assertThat(asset.durationSec()).isEqualTo(3);
//    }
//
////    @Test
////    @DisplayName("생성자: displayOrder 음수면 IllegalArgumentException")
////    void constructor_negativeDisplayOrder_illegalArgument() {
////        assertThatThrownBy(() ->
////                new PostAsset(
////                        PostId.newId(),
////                        MediaType.IMAGE,
////                        -1,
////                        new Url("https://x/img.jpg"),
////                        null, "image/jepg",
////                        10,
////                        10,
////                        null,
////                        null,
////                        null
////                )
////        )
////                .isInstanceOf(IllegalArgumentException.class)
////                .hasMessageContaining("displayOrder");
////    }
//
//    @Test
//    @DisplayName("changeDisplayOrder: 음수 입력 시 InvalidDisplayOrder")
//    void changeDisplayOrder_invalid() {
//        var a = PostAsset.image(
//                PostId.newId(),
//                0,
//                "https://x/i.jpg",
//                null,
//                "image/jpeg",
//                10,
//                10,
//                null,
//                null
//        );
//
//        assertThatThrownBy(() -> a.changeDisplayOrder(-1))
//                .isInstanceOf(InvalidDisplayOrder.class);
//    }
//
//    @Test
//    @DisplayName("changeMeta: 이미지에서 durationSec이 주어지면 InvalidMediaMetadata")
//    void changeMeta_image_durationNotAllowed() {
//        var a = PostAsset.image(
//                PostId.newId(),
//                0,
//                "https://x/i.jpg",
//                null,
//                "image/jpeg",
//                10,
//                10,
//                null,
//                null
//        );
//
//        assertThatThrownBy(() -> a.changeMeta(20, 20, 1, "image/jpeg"))
//                .isInstanceOf(InvalidMediaMetadata.class)
//                .hasMessageContaining("non-VIDEO");
//    }
//
//    @Test
//    @DisplayName("changeMeta: 비디오에서 durationSec 누락/음수면 InvalidMediaMetadata")
//    void changeMeta_video_durationRequiredAndNonNegative() {
//        var a = PostAsset.video(PostId.newId(), 0, "https://x/v.mp4", null,
//                "video/mp4", 10, 10, 5, "alt", "cap");
//
//        assertThatThrownBy(() -> a.changeMeta(10, 10, null, "video/mp4"))
//                .isInstanceOf(InvalidMediaMetadata.class)
//                .hasMessageContaining("must be provided");
//
//        assertThatThrownBy(() -> a.changeMeta(10, 10, -1, "video/mp4"))
//                .isInstanceOf(InvalidMediaMetadata.class)
//                .hasMessageContaining(">=");
//    }
//
//    @Test
//    @DisplayName("changeMeta: width/height 음수는 InvalidMediaMetadata")
//    void changeMeta_negativeWidthHeight_invalid() {
//        var a = PostAsset.image(PostId.newId(), 0, "https://x/i.jpg", null,
//                "image/jpeg", 10, 10, null, null);
//
//        assertThatThrownBy(() -> a.changeMeta(-1, 10, null, "image/jpeg"))
//                .isInstanceOf(InvalidMediaMetadata.class);
//
//        assertThatThrownBy(() -> a.changeMeta(10, -1, null, "image/jpeg"))
//                .isInstanceOf(InvalidMediaMetadata.class);
//    }
//
//    @Test
//    @DisplayName("changeSrcUrl/ThumbUrl/Text: 정상 갱신 및 thumbUrl null 허용")
//    void changeUrlsAndTexts_ok() {
//        var a = PostAsset.image(PostId.newId(), 0, "https://x/i.jpg", "https://x/t.jpg",
//                "image/jpeg", 10, 10, "alt", "cap");
//
//        a.changeSrcUrl("https://x/new.jpg");
//        a.changeThumbUrl(null); // 제거
//        a.changeTexts("ALT2", "CAP2");
//
//        assertThat(a.srcUrl().value()).endsWith("new.jpg");
//        assertThat(a.thumbUrl()).isNull();
//        assertThat(a.altText()).isEqualTo("ALT2");
//        assertThat(a.caption()).isEqualTo("CAP2");
//    }
}
