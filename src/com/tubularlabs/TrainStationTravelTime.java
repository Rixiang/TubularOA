package com.tubularlabs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Solution of Tubular Labs's coding challenge -- TrainStationTravelTime
 *
 * @author Xiang Li (xl68@rice.edu)
 *
 * @version 1.0
 */
public class TrainStationTravelTime {

    /**
     * This program can calculate travel time among different train stations,
     * given timetable of adjacent train stations. Input parameters are passed
     * as stdin from command line. Output results of travel time are printed to stdout.
     *
     * Specifically:
     * 1. reading inputs is with time complexity 0(N);
     * 2. initializing and building the time matrix is with time complexity O(N^2);
     * 3. query each popular route takes O(1) operations, with M queries in total.
     *
     * As a result, this program as a whole takes O(N^2) as time complexity, and O(N^2) as space complexity.
     *
     * @param args program arguments, which can be empty
     * @throws Exception when input data is invalid or stream I/O meets problem
     */
    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            // read inputs from stdin
            inputStreamReader = new InputStreamReader(System.in);
            bufferedReader = new BufferedReader(inputStreamReader);

            int numStation = Integer.valueOf(bufferedReader.readLine());
            if (numStation < 0) {
                throw new RuntimeException("Inputs are wrong! " +
                        "Number of stations should be a positive integer.");
            }


            // initialRoutes contains all routes of stations with distance 1, as given in input
            Map<Integer, HashSet<Integer>> initialRoutes = new HashMap<>();

            int[][] times = new int[numStation][numStation];
            for (int i = 0; i < numStation - 1; i++) {
                // read integers
                String tuple = bufferedReader.readLine();
                String[] split = tuple.split("\\s+");
                int stationA = Integer.valueOf(split[0]);
                int stationB = Integer.valueOf(split[1]);
                int time = Integer.valueOf(split[2]);

                // update times matrix
                times[stationA-1][stationB-1] = time;
                times[stationB-1][stationA-1] = time;

                // add this route to initialRoutes map
                updateRouteMap(initialRoutes, stationA, stationB);
            }

            // read input queries of popular routes
            int numRoute = Integer.valueOf(bufferedReader.readLine());
            if (numRoute < 0) {
                throw new RuntimeException("Inputs are wrong! " +
                        "Number of popular routes should be a non-negative integer.");
            }
            int[][] routes = new int[numRoute][];
            for (int i = 0; i < numRoute; i++) {
                String tuple = bufferedReader.readLine();
                String[] split = tuple.split("\\s+");
                int stationA = Integer.valueOf(split[0]);
                int stationB = Integer.valueOf(split[1]);
                routes[i] = new int[]{stationA-1, stationB-1};
            }

            // get travel time of all possible routes with O(n^2) time complexity
            buildTimeMatrix(times, initialRoutes);

            // write output with O(1) time complexity
            for (int i = 0; i < numRoute; i++) {
                int time = times[routes[i][0]][routes[i][1]];
                System.out.println(time);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (inputStreamReader != null){
                inputStreamReader.close();
            }
            if (bufferedReader != null){
                bufferedReader.close();
            }
        }
    }

    /**
     * This method uses dynamic programming algorithm to build the two-dimension
     * time matrix of N stations. In specific, this matrix is built according the distance of
     * train stations, from adjacent stations, to stations of distance 2,
     * to stations of distance 3, ..., and to stations of distance N-1. Finally,
     * travel time of all possible routes are calculated and stored in this matrix.
     *
     *
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     *
     * @param times travel times among stations, with only time among adjacent stations
     *             is provided.
     * @param initialRoutes routes whose travel time have already been calculated
     */
    private static void buildTimeMatrix(int[][] times, Map<Integer, HashSet<Integer>> initialRoutes) {
        Map<Integer, HashSet<Integer>> prevRoutes = new HashMap<>(initialRoutes);

        int addedRoute = times.length -1;
        // starting with distance = 2, and ending with distance = N - 1
        // N-2 iterates in total
        for (int i = 1; i < times.length - 1; i++){
            // for each distance, create a map that stores routes with exact this travel time
            Map<Integer, HashSet<Integer>> newRoutes = new HashMap<>();

            // For all routes in initialRoutes, find whether it can expand with prevRoutes
            // Actually this while loop is with time complexity around O(n).
            // This is because within each distance, each route initialRoutes is
            // only traversed once. And after all (N-2) steps, only around n^2 routes
            // are added to different newRoutes map, with each only being added once.
            for (Map.Entry<Integer, HashSet<Integer>> entry : initialRoutes.entrySet()){
                int positionA = entry.getKey();
                HashSet<Integer> positionBList = entry.getValue();
                for (int positionB : positionBList){
                    if (prevRoutes.containsKey(positionB)){
                        HashSet<Integer> positionCList = prevRoutes.get(positionB);
                        for (int positionC : positionCList){
                            // avoid overriding correct times for routes
                            if (times[positionA-1][positionC-1] != 0 || positionA == positionC){
                                continue;
                            }
                            // update times matrix
                            times[positionA-1][positionC-1] = times[positionA-1][positionB-1]
                                    + times[positionB-1][positionC-1];
                            times[positionC-1][positionA-1] = times[positionA-1][positionC-1];

                            // update current route map
                            updateRouteMap(newRoutes, positionA, positionC);
                            addedRoute = addedRoute + 2;
                            if (addedRoute == times.length * (times.length - 1)){
                                return;
                            };
                        }
                    }
                }
            }
            prevRoutes = new HashMap<>(newRoutes);
        }
    }

    /**
     * Add route from positionA to positionB, and vice versa, to given route map.
     *
     * @param routes        route map that contains all routes between stations with same distance
     * @param positionA     station A
     * @param positionB     station B
     */
    private static void updateRouteMap(Map<Integer, HashSet<Integer>> routes,
                                       int positionA, int positionB){
        HashSet<Integer> newPositionCSet;
        if (routes.containsKey(positionA)){
            newPositionCSet = routes.get(positionA);
        }else{
            newPositionCSet = new HashSet<>();
        }
        newPositionCSet.add(positionB);
        routes.put(positionA, newPositionCSet);

        // add the reverse route to route map
        HashSet<Integer> newPositionASet;
        if (routes.containsKey(positionB)){
            newPositionASet = routes.get(positionB);
        }else{
            newPositionASet = new HashSet<>();
        }
        newPositionASet.add(positionA);
        routes.put(positionB, newPositionASet);
    }
}
