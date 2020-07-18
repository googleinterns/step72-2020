package com.google.sps.data;

import java.util.ArrayList;
import java.util.HashMap;

// public class WaterContaminant {

//     private int contaminantCode;
//     private String contaminantName;
//     private String sources;
//     private String definition;
//     private String healthEffects;
//     private HashMap<String, ArrayList<String>> violations;



//     public WaterContaminant(final String[] sdwViolationCells) {
//         contaminantCode = Integer.parseInt(sdwViolationCells[6]);
//         contaminantName = sdwViolationCells[7];
//         sources = sdwViolationCells[8];
//         definition = sdwViolationCells[9];
//         healthEffects = sdwViolationCells[10];
//         violations = new HashMap<String, ArrayList<String>>();
//     }

//     public void addViolationInstance(String violationDate, String enforcementAction) {
//         if(violationDate == null || enforcementAction == null){
//             System.out.println("Violation date or action is null");
//             return;
//         }
//         violations.putIfAbsent(violationDate, new ArrayList<String>());
//         if(!violations.get(violationDate).contains(enforcementAction)){
//             violations.get(violationDate).add(enforcementAction);
//         }
//     }

//     public String toString() {
//         return contaminantName + " during " + violations.toString();
//     }


    
// }

