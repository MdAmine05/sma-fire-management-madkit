# SMA Fire Management — MadKit + AALAADIN/AGR

## 1. Project Description

This project implements a **Multi-Agent System (SMA)** for the management of a forest fire scenario.

The project is based on:

- **Platform:** MadKit 5.3.2
- **Methodology:** AALAADIN / AGR
- **Language:** Java 17
- **IDE:** IntelliJ IDEA
- **Case study:** Intelligent forest fire detection, risk evaluation, intervention, and evacuation

The goal is to show how several autonomous agents can cooperate through an organizational model based on:

```text
Agent → Group → Role
```

The simulation is dynamic: the sensor agent periodically observes different forest zones, detects risky situations, sends alerts, and other agents react according to their roles.

---

## 2. Project Objective

The system simulates the following emergency process:

1. A sensor observes forest zones.
2. If temperature and smoke indicate danger, an alert is sent.
3. The coordinator asks the drone to confirm the fire.
4. The weather agent provides weather data.
5. The propagation agent calculates the risk level.
6. The coordinator sends intervention orders.
7. The firefighter agent intervenes if available.
8. The evacuation agent triggers evacuation if houses are nearby.
9. The interface agent displays the global state and event timeline.

---

## 3. AALAADIN / AGR Organization

The project follows the **AGR model**:

| AGR Concept | Meaning in this project |
|---|---|
| Agent | Autonomous entity such as sensor, drone, firefighter |
| Group | Organizational context such as surveillance or coordination |
| Role | Function played by an agent inside a group |
| Community | Global system containing all groups |

### Community

```text
ForetCommunity
```

### Groups

| Group | Purpose |
|---|---|
| GroupeSurveillance | Detection and observation |
| GroupeCoordination | Decision-making and message routing |
| GroupeIntervention | Firefighter intervention |
| GroupeSecurite | Civil evacuation and safety |

### Agents and Roles

| Agent | Main Role | Group |
|---|---|---|
| AgentCapteur | Detecteur | GroupeSurveillance |
| AgentDrone | Observateur / Guide | GroupeSurveillance / GroupeIntervention |
| AgentMeteo | FournisseurContexte | GroupeSurveillance |
| AgentPropagation | AnalysteRisque | GroupeCoordination |
| AgentCoordinateur | Coordinateur / Superviseur | GroupeCoordination / GroupeSecurite |
| AgentPompier | Intervenant | GroupeIntervention |
| AgentEvacuation | ResponsableEvacuation | GroupeSecurite |
| AgentInterface | InterfaceObserver | GroupeCoordination |

### Technical MadKit Adaptation

In the conceptual AGR model, agents belong to their natural functional groups.  
In the implementation, some agents also register a communication role inside `GroupeCoordination`.

Reason:

> MadKit role-based communication may require the sender to participate in the target group.  
> Therefore, additional communication roles are registered in `GroupeCoordination` to make the prototype reliable and simple.

This does **not** change the conceptual AGR design. It is an implementation adaptation.

---

## 4. Implemented Agents

### AgentCapteur

The sensor agent periodically observes different forest zones.

It checks:

- temperature
- smoke presence
- proximity to houses

If:

```text
temperature > 60°C AND smoke = true
```

then it sends an `AlerteIncendie` message to the coordinator.

It also sends observation logs to `AgentInterface` so the dashboard can show the simulation timeline.

### AgentCoordinateur

The coordinator is the central decision agent.

It receives alerts and coordinates the scenario:

1. receives fire alert
2. asks the drone for confirmation
3. asks the weather agent for data
4. asks the propagation agent for risk analysis
5. sends intervention order
6. sends evacuation order if necessary
7. updates the dashboard

### AgentDrone

The drone receives a fire alert and confirms the fire.

It sends:

```text
ConfirmationIncendie
```

to the coordinator.

### AgentMeteo

The weather agent provides environmental data:

```text
Wind = FORT
Humidity = 20%
```

It sends:

```text
DonneesMeteo
```

to the coordinator.

### AgentPropagation

The propagation agent calculates the fire risk score using `RiskCalculator`.

It sends:

```text
RisquePropagation
```

to the coordinator.

### AgentPompier

The firefighter agent handles intervention.

It has an internal availability state:

```text
AVAILABLE → BUSY → AVAILABLE
```

When it receives an intervention order, it becomes busy during the intervention, then becomes available again.

This models a simple resource-management constraint.

### AgentEvacuation

The evacuation agent reacts to intervention orders.

If the zone is close to houses:

```text
procheHabitations = true
```

then it triggers a civil evacuation alert.

If houses are not nearby, it reports that evacuation is not necessary.

### AgentInterface

The interface agent displays a custom Swing dashboard with:

- current status
- current zone
- risk level
- risk score
- color indicator
- live timeline of agent messages

The dashboard receives updates from the coordinator and sensor agents.

---

## 5. Message Classes

The project defines several message objects used between agents.

| Message | Sender | Receiver | Purpose |
|---|---|---|---|
| AlerteIncendie | AgentCapteur | AgentCoordinateur | Fire alert |
| ConfirmationIncendie | AgentDrone | AgentCoordinateur | Fire confirmation |
| DonneesMeteo | AgentMeteo | AgentCoordinateur | Weather information |
| RisquePropagation | AgentPropagation | AgentCoordinateur | Risk score and level |
| OrdreIntervention | AgentCoordinateur | AgentPompier / AgentEvacuation | Intervention and evacuation order |
| SMAFireMessage | Any agent | Any agent | MadKit message wrapper |

MadKit messages must extend `madkit.kernel.Message`, so `SMAFireMessage` is used as a wrapper around project-specific message objects.

---

## 6. Risk Calculation

The risk score is calculated by `RiskCalculator`.

The score uses normalized values:

| Variable | Meaning |
|---|---|
| Temperature | Fire intensity indicator |
| Smoke | Fire detection indicator |
| Wind | Propagation factor |
| House proximity | Human risk factor |
| Available firefighters | Resource factor |

Formula:

```text
ScoreRisk =
  0.30 * Temperature
+ 0.25 * Smoke
+ 0.20 * Wind
+ 0.15 * HouseProximity
- 0.10 * AvailableResources
```

Risk levels:

| Score | Level |
|---|---|
| 0.00 – 0.30 | FAIBLE |
| 0.31 – 0.60 | MOYEN |
| 0.61 – 0.80 | ELEVE |
| 0.81 – 1.00 | CRITIQUE |

---

## 7. Dynamic Simulation Scenario

The sensor agent sends several observations over time.

### Observation 1

```text
Zone 1
Temperature = 42°C
Smoke = false
Houses nearby = false
Result: normal situation, no alert
```

### Observation 2

```text
Zone 3
Temperature = 78°C
Smoke = true
Houses nearby = true
Result: critical fire, intervention + evacuation
```

### Observation 3

```text
Zone 5
Temperature = 66°C
Smoke = true
Houses nearby = false
Result: high risk, intervention without evacuation
```

### Observation 4

```text
Zone 2
Temperature = 88°C
Smoke = true
Houses nearby = true
Result: critical fire, intervention + evacuation
```

---

## 8. Example Console Output

A typical execution produces this type of output:

```text
=== Fire Simulation Launcher ===
[AgentCoordinateur] Roles Coordinateur and Superviseur joined.
[AgentDrone] Role Observateur joined.
[AgentMeteo] Role FournisseurContexte joined.
[AgentPropagation] Role AnalysteRisque joined.
[AgentPompier] Role Intervenant joined. Etat=AVAILABLE
[AgentEvacuation] Role ResponsableEvacuation joined.
[AgentInterface] Dashboard active.
[AgentCapteur] Role Detecteur joined.

[AgentCapteur] Nouvelle observation : Zone 3 | temperature=78.0°C | fumee=true | procheHabitations=true
[AgentCapteur] Zone 3 : temperature=78.0°C, fumee=true -> ALERTE envoyee
[AgentCoordinateur] Alerte recue. Demande de confirmation au drone.
[AgentDrone] Zone 3 inspectee -> Incendie CONFIRME.
[AgentCoordinateur] Incendie confirme. Demande des donnees meteo.
[AgentMeteo] Vent=FORT, humidite=20%
[AgentCoordinateur] Donnees meteo recues. Demande analyse propagation.
[AgentPropagation] Zone 3 ScoreRisque=81 -> Niveau CRITIQUE
[AgentCoordinateur] Priorite CRITIQUE. Envoi pompiers + evacuation si necessaire.
[AgentPompier] Intervention lancee sur Zone 3 avec priorite CRITIQUE. Etat=BUSY
[AgentEvacuation] Alerte civile declenchee pour Zone 3.
[AgentPompier] Intervention terminee sur Zone 3. Etat=AVAILABLE
```

---

## 9. Dashboard

The project includes a custom Swing dashboard implemented by `AgentInterface`.

It displays:

- system status
- current zone
- current risk level
- risk score
- risk indicator color
- live agent timeline

The dashboard is **not** the default MadKit GUI.  
It is a custom interface agent created for this simulation.

The default MadKit window may appear empty because it is only the platform shell. The real dashboard is the Swing window created by `AgentInterface`, and detailed logs are also visible in the IntelliJ console.

---

## 10. Project Structure

```text
sma-fire-management-madkit/
│
├── lib/
│   └── madkit-5.3.2.jar
│
├── src/
│   └── fr/sma/fire/
│       ├── agents/
│       │   ├── AgentCapteur.java
│       │   ├── AgentCoordinateur.java
│       │   ├── AgentDrone.java
│       │   ├── AgentMeteo.java
│       │   ├── AgentPropagation.java
│       │   ├── AgentPompier.java
│       │   ├── AgentEvacuation.java
│       │   └── AgentInterface.java
│       │
│       ├── config/
│       │   └── AGRConfig.java
│       │
│       ├── launcher/
│       │   ├── FireSimulationLauncher.java
│       │   ├── SetupTest.java
│       │   ├── MessageTest.java
│       │   └── RiskCalculatorTest.java
│       │
│       ├── logic/
│       │   └── RiskCalculator.java
│       │
│       ├── messages/
│       │   ├── AlerteIncendie.java
│       │   ├── ConfirmationIncendie.java
│       │   ├── DonneesMeteo.java
│       │   ├── RisquePropagation.java
│       │   ├── OrdreIntervention.java
│       │   └── SMAFireMessage.java
│       │
│       └── model/
│           └── ZoneForet.java
│
├── screenshots/
├── diagrams/
├── rapport/
├── presentation/
├── README.md
└── .gitignore
```

---

## 11. How to Run

### Requirements

- Java JDK 17
- IntelliJ IDEA
- MadKit 5.3.2 JAR inside `lib/`

### Run from IntelliJ

Open:

```text
src/fr/sma/fire/launcher/FireSimulationLauncher.java
```

Run the `main()` method.

### Main class

```text
fr.sma.fire.launcher.FireSimulationLauncher
```

---

## 12. Tests

The project includes simple test launchers.

| Test Class | Purpose |
|---|---|
| SetupTest | Verifies project setup and model creation |
| MessageTest | Verifies message classes |
| RiskCalculatorTest | Verifies risk score calculation |
| FireSimulationLauncher | Runs the full MadKit simulation |

---

## 13. Demo Plan

For the video/demo:

1. Show project structure in IntelliJ.
2. Show `AGRConfig.java` to explain community, groups, and roles.
3. Show the agent classes briefly.
4. Run `FireSimulationLauncher`.
5. Show the Swing dashboard.
6. Show the IntelliJ console output.
7. Explain one full scenario:
   - detection
   - confirmation
   - weather data
   - risk calculation
   - intervention
   - evacuation

---

## 14. Important Implementation Notes

### MadKit + AGR

Agents communicate using MadKit and AGR concepts.

The general logic is:

```text
Community → Group → Role → Message
```

Example:

```text
AgentCapteur sends an alert to the Coordinateur role in GroupeCoordination.
```

### Technical Adaptation

MadKit may require the sender to be part of the group where role-based communication occurs.

For this reason, some agents request an additional role inside `GroupeCoordination`.

This is an implementation adaptation and does not change the conceptual AGR design.

### Dashboard

The dashboard is implemented as an agent:

```text
AgentInterface
```

It receives updates through the role:

```text
InterfaceObserver
```

---

## 15. GitHub Workflow

Recommended workflow:

```bash
git status
git add .
git commit -m "feat: update dynamic MadKit fire simulation"
git push
```

Avoid pushing:

```text
out/
.idea/
*.class
```

These files should be ignored by `.gitignore`.

---

## 16. Current Status

Implemented:

- Java project setup
- MadKit library integration
- AGR constants
- forest zone model
- message classes
- risk calculation logic
- multiple autonomous agents
- dynamic multi-zone simulation
- firefighter availability state
- evacuation logic
- Swing dashboard
- console logs
- test launchers

The project is ready for:

- screenshots
- report writing
- diagrams
- presentation
- video demo
