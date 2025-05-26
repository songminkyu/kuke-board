package kuke.board.articleread.service.event.handler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {

    //이벤트를 햌들 처리하는 용도
    void handle(Event<T> event);

    //이벤트 지원 여부를 확인하기위한 용도
    boolean supports(Event<T> event);
}
