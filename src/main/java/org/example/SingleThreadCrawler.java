package org.example;


import org.example.queue.CustomQueue;
import org.example.queue.Queue;
import org.example.set.CoarseHashSet;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.join;


public class SingleThreadCrawler {
    private final Queue<Node> searchQueue = new CustomQueue<>();
    private final org.example.set.HashSet<String> visited = new CoarseHashSet<>(1000);
    private final WikiClient client = new WikiClient();

    public static void main(String[] args) throws Exception {
        SingleThreadCrawler crawler = new SingleThreadCrawler();

        long startTime = System.nanoTime();
        // Лимит для потоков определил методом "тыка", чтобы не попасть под rate limiter википедии (примерно не более 200 запросов в секунду)
        String result = crawler.find("Java_(programming_language)", "Cat", 3, TimeUnit.MINUTES, 200, 2);
        long finishTime = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

        System.out.println("Took " + finishTime + " seconds, result is: " + result);
    }

    public String find(String from, String target, long timeout, TimeUnit timeUnit, int threadLimit, int searchLevels) throws Exception {
        long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
        searchQueue.add(new Node(from, null, from, from));
        Node result = null;
        var level = -1;

        while (result == null && !searchQueue.isEmpty() && level <= searchLevels) {
            level++;
            System.out.println("Search level: " + level);
            if (deadline < System.nanoTime()) {
                throw new TimeoutException();
            }

            Set<Crawler> threads = new HashSet<>();
            Set<Crawler> joinedThreads = new HashSet<>();
            while (!searchQueue.isEmpty() && result == null) {
                if (deadline < System.nanoTime()) {
                    throw new TimeoutException();
                }
                var node = searchQueue.poll();
                var crawler = new Crawler(node, visited, client);
                crawler.start();
                threads.add(crawler);
                if (threads.size() > threadLimit) {
                    result = getResultFromThreads(threads, target);
                    joinedThreads.addAll(threads);
                    threads.clear();
                    Thread.sleep(1500L);
                }
            }

            if (result == null) {
                result = getResultFromThreads(threads, target);
            }
            if (result == null) {
                joinedThreads.addAll(threads);
                for (var thread : joinedThreads) {
                    thread.getResultNodes().forEach(searchQueue::add);
                }
            }
        }

        if (result != null) {
            List<String> resultList = new ArrayList<>();
            Node search = result;
            while (true) {
                resultList.add(search.getLink());
                if (search.getNext() == null) {
                    break;
                }
                search = search.getNext();
            }
            Collections.reverse(resultList);

            return join(" > ", resultList);
        }

        return "not found";
    }

    private Node getResultFromThreads(Set<Crawler> threads, String target) throws InterruptedException {
        Node result = null;
        Set<Crawler> joinedThreads = new HashSet<>();
        while (threads.size() != joinedThreads.size()) {
            for (var thread : threads) {
                if (!joinedThreads.contains(thread) && !thread.isAlive()) {
                    thread.join();
                    joinedThreads.add(thread);
                    if (result == null) {
                        var nodes = thread.getResultNodes();
                        for (var node : nodes) {
                            if (node.getLink().equalsIgnoreCase(target) || node.getTitle().equalsIgnoreCase(target) || node.getText().equalsIgnoreCase(target)) {
                                result = node;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
