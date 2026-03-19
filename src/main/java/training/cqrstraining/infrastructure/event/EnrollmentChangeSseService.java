package training.cqrstraining.infrastructure.event;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import training.cqrstraining.api.EnrollmentChangeNotification;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnrollmentChangeSseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        String emitterId = UUID.randomUUID().toString();
        emitters.put(emitterId, emitter);

        emitter.onCompletion(() -> emitters.remove(emitterId));
        emitter.onTimeout(() -> emitters.remove(emitterId));
        emitter.onError(error -> emitters.remove(emitterId));

        return emitter;
    }

    public void publish(Long courseId, Long version) {
        EnrollmentChangeNotification notification = new EnrollmentChangeNotification(courseId, version);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("enrollment-changed")
                        .id(courseId + "-" + version)
                        .data(notification));
            } catch (IOException ex) {
                emitter.complete();
                emitters.remove(id);
            }
        });
    }
}


