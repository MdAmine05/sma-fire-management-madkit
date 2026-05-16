# SMA Fire Management — MadKit + AALAADIN/AGR

## Description
This project models a multi-agent system for forest fire management using the AALAADIN/AGR methodology and the MadKit platform.

## Case Study
The system simulates the detection and management of a forest fire using several autonomous agents.

## Main Agents
- AgentCapteur
- AgentDrone
- AgentMeteo
- AgentPropagation
- AgentCoordinateur
- AgentPompier
- AgentEvacuation
- AgentInterface

## Main AGR Structure
- Community: ForetCommunity
- Groups:
  - GroupeSurveillance
  - GroupeCoordination
  - GroupeIntervention
  - GroupeSecurite

## Current Status

### Step 1: Project setup
Done:
- Java project created with IntelliJ.
- Package structure created.
- `AGRConfig` contains official AGR names.
- `ZoneForet` represents a forest zone.
- `SetupTest` validates the setup.

### Step 2: Message classes
Done:
- `AlerteIncendie`
- `ConfirmationIncendie`
- `DonneesMeteo`
- `RisquePropagation`
- `OrdreIntervention`

These classes represent the communication contract between agents.

## Scenario Flow

1. `AgentCapteur` detects high temperature and smoke.
2. It sends `AlerteIncendie`.
3. `AgentDrone` confirms the fire using `ConfirmationIncendie`.
4. `AgentMeteo` provides `DonneesMeteo`.
5. `AgentPropagation` calculates `RisquePropagation`.
6. `AgentCoordinateur` sends `OrdreIntervention`.