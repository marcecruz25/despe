package com.despe.despe.service.impl;

import com.despe.despe.model.Line;
import com.despe.despe.model.RequestObject;
import com.despe.despe.model.Root;
import com.despe.despe.service.DespegarService;
import com.google.gson.Gson;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by marcelo.cruz on 29/03/18.
 */

@Service
public class DespegarServiceImpl implements DespegarService {

    private String id = "";

    private List<Line> lineList = new ArrayList<>();

    @Override
    public void startApp() {

        System.out.println("Please type the file path and name (example /filepath/filename:");
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.next();
        /*Gson gson = new Gson();
        final Line myClass = gson.fromJson(fileName.toString(), Line.class);*/
        StopWatch timer = StopWatch.createStarted();

        try(Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(this::saveLineReaded);
            createJson();
        } catch (IOException io){
            io.printStackTrace();
        }

        timer.stop();
        System.out.println("App executed in : " + timer.getTime(TimeUnit.MILLISECONDS) + " ms");
        scanner.close();
    }


    public void saveLineReaded(String lineString) {
        try {

            String[] parsedLine = lineString.split(" ");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            Line line = new Line();
            line.setStart(formatter.parse(parsedLine[0].length() < 24 ? parsedLine[0].replace("Z", ".000Z") : parsedLine[0]));
            line.setEnd(formatter.parse(parsedLine[1].length() < 24 ? parsedLine[1].replace("Z", ".000Z") : parsedLine[1]));
            line.setService(parsedLine[3]);
            line.setSpan(parsedLine[4]);

            if(id.isEmpty() || id.equalsIgnoreCase(parsedLine[2])) {
                id = parsedLine[2];
                lineList.add(line);
            } else {
                createJson();
                id = "";
                lineList.add(line);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void createJson() {

        Collections.reverse(lineList);
        RequestObject obj = new RequestObject();
        obj.setId(id);
        Root root = new Root();
        root.setStart(lineList.get(0).getStart());
        root.setEnd(lineList.get(0).getEnd());
        root.setService(lineList.get(0).getService());
        Line l = lineList.get(0);
        root.setSpan(lineList.get(0).getSpan()[1]);
        lineList.remove(0);
        List<Root> listRoot = new ArrayList<>();

        while (!lineList.isEmpty()) {

            final String sb = l.getSpan()[1];

            List<Line> tmpLines = lineList.stream().filter(r -> r.getSpan()[0].startsWith(sb.toString())).collect(Collectors.toList());
            if (!tmpLines.isEmpty()) {
                tmpLines.forEach(x -> listRoot.add(convertToRoot(x)));
                listRoot.forEach(x -> x.setCalls(findLeafs(x)));
                lineList.removeAll(tmpLines);
            } else {
                listRoot.add(convertToRoot(lineList.get(0)));
                lineList.remove(0);
            }

            l = lineList.isEmpty() == true ? null : lineList.get(0);
        }
        root.setCalls(listRoot);
        obj.setRoot(root);
        System.out.println(obj.toString() + "\n");
    }


    public List<Root> findLeafs (Root line) {

        final String span = line.getSpan();
        List <Line> tmpLines = lineList.stream().filter(l -> l.getSpan()[0].startsWith(span)).collect(Collectors.toList());
        List<Root> leafs = new ArrayList<>();
        tmpLines.forEach(x -> leafs.add(convertToRoot(x)));
        leafs.forEach(x -> x.setCalls(findLeafs(x)));
        lineList.removeAll(tmpLines);
        return leafs;
    }


    public Root convertToRoot(Line line) {
        Root leaf = new Root();
        leaf.setStart(line.getStart());
        leaf.setEnd(line.getEnd());
        leaf.setSpan(line.getSpan()[1]);
        leaf.setService(line.getService());
        return leaf;
    }
}
