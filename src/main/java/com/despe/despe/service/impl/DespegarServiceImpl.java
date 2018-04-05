package com.despe.despe.service.impl;

import com.despe.despe.model.*;
import com.despe.despe.service.DespegarService;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by marcelo.cruz on 29/03/18.
 */

@Service
public class DespegarServiceImpl implements DespegarService {

    private List<LineBack> lineList = new ArrayList<>();
    private FileWriter fichero;
    private BufferedWriter bufferedWriter;
    private static int linesSuccess = 0;
    private static int linesFailure = 0;
    HashMap<String, List<Line>> lines = new HashMap<>();

    @Override
    public void startApp() throws IOException {
        processMain();
    }

    private void processMain() throws IOException {
        try {
            System.out.println("Please type the file path and name (example /filepath/filename.txt:");
            Scanner scanner = new Scanner(System.in);
            String fileName = scanner.next();
            System.out.println("Please type the file path and name to save of Json (example /filepath/filename.txt:");
            String fileNameToSave = scanner.next();
            StopWatch timer = StopWatch.createStarted();
            fichero = new FileWriter(fileNameToSave);
            bufferedWriter = new BufferedWriter(fichero);

            Stream<String> stream = Files.lines(Paths.get(fileName));
            stream.forEach(this::saveLineReaded);

            processListLine();
            prepareToCreateJson();
            timer.stop();
            System.out.println("App executed in : " + timer.getTime(TimeUnit.MILLISECONDS) + " ms");
            System.out.println("App executed total of traces " + (linesSuccess + linesFailure) + ", " + linesSuccess + " was success and " + linesFailure + " failure");
            scanner.close();
        } catch (IOException io){
            io.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bufferedWriter) {
                bufferedWriter.close();
            }
            if (null != fichero) {
                fichero.close();
            }
        }
    }

    private void saveLineReaded(String lineString) {
        try {
            String[] parsedLine = lineString.split(" ");
            if (isValidData(parsedLine)) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                LineBack line = new LineBack();
                line.setStart(formatter.parse(parsedLine[0].length() < 24 ? parsedLine[0].replace("Z", ".000Z") : parsedLine[0]));
                line.setEnd(formatter.parse(parsedLine[1].length() < 24 ? parsedLine[1].replace("Z", ".000Z") : parsedLine[1]));
                line.setService(parsedLine[3]);
                line.setSpan(parsedLine[4]);
                line.setId(parsedLine[2]);
                lineList.add(line);
            } else {
                linesFailure++;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createJson(String id, List<Line> lineList) throws IOException {
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
                listRoot.forEach(x -> x.setCalls(findLeafs(x, lineList)));
                lineList.removeAll(tmpLines);
            } else {
                listRoot.add(convertToRoot(lineList.get(0)));
                lineList.remove(0);
            }

            l = lineList.isEmpty() == true ? null : lineList.get(0);
        }
        root.setCalls(listRoot);
        obj.setRoot(root);
        bufferedWriter.write(obj.toString() + "\n");
        System.out.println(obj.toString() + "\n");
    }

    private List<Root> findLeafs (Root line, List<Line> lineLists) {

        final String span = line.getSpan();
        List <Line> tmpLines = lineLists.stream().filter(l -> l.getSpan()[0].startsWith(span)).collect(Collectors.toList());
        List<Root> leafs = new ArrayList<>();
        tmpLines.forEach(x -> leafs.add(convertToRoot(x)));
        leafs.forEach(x -> x.setCalls(findLeafs(x, lineLists)));
        lineLists.removeAll(tmpLines);
        return leafs;
    }

    private Root convertToRoot(Line line) {
        Root leaf = new Root();
        leaf.setStart(line.getStart());
        leaf.setEnd(line.getEnd());
        leaf.setSpan(line.getSpan()[1]);
        leaf.setService(line.getService());
        return leaf;
    }

    /*
    *
    * Se considera valido solo si contiene 6 elementos y ninguno de ellos viene vacio. *
    * */
    private boolean isValidData(String[] parsedLine) {
        if (parsedLine.length != 5) return false;

        for (String valores : parsedLine) {
            if (valores.isEmpty()) return false;
        }
        return true;
    }

    private void processListLine() {
        for (LineBack linea : lineList) {
            if (lines.containsKey(linea.getId())) {
                List<Line> salvar = lines.get(linea.getId());
                Line line = new Line();
                line.setEnd(linea.getEnd());
                line.setService(linea.getService());
                line.setSpan(linea.getSpan());
                line.setStart(linea.getStart());
                salvar.add(line);
                lines.put(linea.getId(), salvar);
            } else {
                List<Line> lib = new ArrayList<>();
                Line line = new Line();
                line.setEnd(linea.getEnd());
                line.setService(linea.getService());
                line.setSpan(linea.getSpan());
                line.setStart(linea.getStart());
                lib.add(line);
                lines.put(linea.getId(), lib);
            }
        }
    }

    private void prepareToCreateJson() throws IOException {
        for (Map.Entry<String, List<Line>> entry : lines.entrySet()) {
            createJson(entry.getKey(), entry.getValue());
            linesSuccess++;
        }
    }
}
