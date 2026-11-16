# Meitheal — Community Time-Bank

COMP41670 Software Engineering (Trimester 1, 2026/27) Group Project

**Group Name:** Unknown

**Members:**
- Ye Yongan (`25267336`, `yong.a.ye@ucdconnect.ie`, `@yongan-ye`)
- Pan Chenhao (`26106009`, `chenhao.pan@ucdconnect.ie`, `@chenhaopanspace`)
- Li Tianyue (`26104007`, `tianyue.li@ucdconnect.ie`, `@tianyueli-netizen`)
- Qian Zhenwei (`26116537`, `zhenwei.qian@ucdconnect.ie`, `@zhenweiqian-art`)
- Kunze Qi (`25262028`, `kunze.qi@ucdconnect.ie`, `@kunzeqi-web`)

## Build, Test, and Run Instructions

This project targets **Java 21** and requires no pre-installed Maven installation when using the bundled Maven Wrapper.

### 1. Compile the project
- **macOS / Linux:** `./mvnw compile`
- **Windows:** `mvnw.cmd compile`

### 2. Run the full test suite (Unit, Invariant & Adversarial tests)
- **macOS / Linux:** `./mvnw clean test`
- **Windows:** `mvnw.cmd clean test`

### 3. Run the CLI application
First compile the classes, then launch `MeithealCli`:
- **macOS / Linux:**
  ```bash
  ./mvnw compile && java -cp target/classes ie.ucd.comp41670.meitheal.app.MeithealCli