package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;

/**
 * 특정 대상(ID)에 대한 나의 투표값을 표현하는 값 객체.
 * <p>value: -1(비추천), +1(추천). 투표가 없을 때는 이 객체 자체가 존재하지 않음(Optional로 처리).</p>
 */
public sealed interface MyVoteValue extends ValueObject permits MyPostVote, MyCommentVote{
    int value();
}
