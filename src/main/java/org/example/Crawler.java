package org.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.set.HashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Crawler extends Thread {
    private final Node node;
    private final HashSet<String> visited;
    private final WikiClient client;
    @Getter
    private final List<Node> resultNodes = new ArrayList<>();
    @Getter
    private long finishTime = -1L;

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            var links = client.getByTitle(node.getLink());
            finishTime = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            if (links.isEmpty()) {
                return;
            }
            for (var key : links.keySet()) {
                var currentLink = key.toLowerCase();
                if (!visited.contains(currentLink)) {
                    visited.add(currentLink);
                    resultNodes.add(new Node(key, node, links.get(key).get(1), links.get(key).get(1)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
