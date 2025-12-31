package com.blue.sample;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.List;

@RestController
public class RandomController {
  
  private final SecureRandom rnd = new SecureRandom();
  
  private static final List<Review> POOL = List.of(
      new Review(1,  "Ari Kim",     "Pilsa is the first club where I felt genuinely welcomed from day one. People say hi, remember you, and mean it."), // 필사는 첫날부터 진짜 환영받는다고 느낀 첫 동아리예요. 다들 인사해주고 기억해주고 진심이에요.
      new Review(2,  "Minseo Park", "The community is warm in a quiet way. Even if you are new, you never feel like an outsider."), // 공동체가 조용하게 따뜻해요. 신입이어도 소외된 느낌이 전혀 없어요.
      new Review(3,  "Joon Lee",    "We do not just write. We go on photo walks, collect inspirations, and turn them into lettering together."), // 글만 쓰는 게 아니라 출사도 나가고, 영감을 모아서 같이 레터링으로 완성해요.
      new Review(4,  "Hana Choi",   "The best part is how people cheer you on. Small progress gets real support, not awkward compliments."), // 사람들이 응원해주는 게 제일 좋아요. 작은 성장도 진짜로 지지해줘요.
      new Review(5,  "Ethan Lim",   "Pilsa feels like a friendly workshop. You can ask anything about pens, paper, and techniques without embarrassment."), // 필사는 친근한 공방 같아요. 펜, 종이, 기법 뭐든 부담 없이 물어볼 수 있어요.
      new Review(6,  "Soyeon Jung", "I joined for handwriting practice, but I stayed because it feels like a small home I can return to."), // 글씨 연습하려고 들어왔는데, 돌아올 곳 같은 작은 집 느낌이라 계속하게 돼요.
      new Review(7,  "Daniel Kang", "Making stationery together was surprising. Seeing our designs become real products was honestly inspiring."), // 같이 문구류를 만드는 게 신기했어요. 우리가 만든 디자인이 실제 제품이 되는 순간이 정말 큰 자극이 됐어요.
      new Review(8,  "Yuna Shin",   "Whether it is a photo walk, a writing session, or making stationery, Pilsa helps me notice small details and turn them into better work."), // 출사든 글쓰기든 문구 제작이든, 필사는 작은 디테일을 더 잘 보게 하고 그걸 더 좋은 작업으로 바꾸게 해줘요.
      new Review(9,  "Mason Hwang", "People share tools and tips generously. It is a club that grows together, not a place to compete."), // 도구도 팁도 아낌없이 나눠요. 경쟁이 아니라 같이 성장하는 동아리예요.
      new Review(10, "Jisoo Han",   "If you are shy, it is still easy to fit in. The kindness here is consistent, not just for show."), // 내향적이어도 잘 녹아들 수 있어요. 여기 친절은 보여주기용이 아니라 꾸준해요.
      new Review(11, "Noah Kim",    "Every session has a calm energy, and after we write, we talk like friends. It is comforting."), // 모임마다 차분한 에너지가 있고, 글 쓰고 나면 친구처럼 이야기해요. 마음이 편해져요.
      new Review(12, "Seulgi Bae",  "The feedback culture is healthy. People point out strengths first, then help you improve one step at a time."), // 피드백 문화가 건강해요. 장점부터 짚어주고 한 걸음씩 개선을 도와줘요.
      new Review(13, "Ryan Woo",    "The projects are fun and real. From lettering pieces to stationery making, you build a portfolio naturally."), // 프로젝트가 재밌고 실전적이에요. 작품부터 문구 제작까지 자연스럽게 포트폴리오가 쌓여요.
      new Review(14, "Mira Song",   "Pilsa celebrates effort. When someone posts a work-in-progress, the comments feel like a warm hug."), // 필사는 노력을 축하해줘요. 작업 과정을 올리면 댓글이 진짜 따뜻하게 안아주는 느낌이에요.
      new Review(15, "Sophia Cho",  "This is a club where people care about people. The art is great, but the community is what makes it special.") // 사람을 진짜로 챙기는 동아리예요. 작업도 좋지만, 공동체가 특별함의 핵심이에요.
  );
  
  @GetMapping(value = "/api/pilsa/random", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Review> random() {
    Review picked = POOL.get(rnd.nextInt(POOL.size()));
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(picked);
  }
  
  public record Review(int id, String name, String review) {}
}
