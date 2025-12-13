
# FlightBookingSystem

这是一个用 Java Swing 实现的简易航班订票演示系统（带 CSV 持久化），包含用户端与管理员端的基本功能：搜索航班、选座、下单、申请退票/改签，以及管理员审批流程。

## 主要特性

- 用户界面：航班搜索、舱位选择、图形化选座、订单查看与管理。
- 管理界面：航班管理、用户管理、订单管理（管理员可审核退票/改签申请）。
- 订单显示：管理员界面中订单表显示用户名（优先显示全名，其次 email），并支持“仅显示待审核”过滤。
- 申请/审批流程：用户端提交退票或改签申请（带可选建议目标航班/座位与理由），管理员审核后才执行实际退款或改签操作。
- UI 改善：搜索区改为可调整大小的分割窗格（`JSplitPane`），并增加“查找全部”按钮以重置过滤器。

## 目录结构（关键文件）

- `src/` - 程序源代码：
  - `App.java` - 应用程序入口（运行主类）
  - `AdminPanel.java` - 管理员界面（航班/用户/订单 管理）
  - `BookingPanel.java` - 航班搜索与下单界面（包含可调节的搜索区）
  - `SeatSelectionDialog.java` - 选座对话框（支持外部预选舱位）
  - `MyOrdersPanel.java` - 用户订单面板（提交退票/改签申请）
  - `Order.java` / `OrderDao.java` - 订单模型与 CSV 持久化
  - `User.java` / `UserDao.java` - 用户模型与持久化
  - `Flight.java` / `FlightDao.java` - 航班模型与持久化

## 运行环境与依赖

- Java 11 或更高（推荐使用 Java 11/17）
- Maven（用于构建）

## 快速构建与运行（PowerShell）

1. 在项目根目录运行打包：

```powershell
mvn package
```

2. 直接用类路径运行（如果没有将程序打成可执行 jar）：

```powershell
java -cp target/classes App
```

3. 或者（若 `pom.xml` 配置了 exec 插件）使用：

```powershell
mvn exec:java -Dexec.mainClass="App"
```

（如果在 IDE 中开发，直接在 IDE 里以 `App` 作为主类运行即可。）

## 数据文件说明

- 本项目采用 CSV 文件做简单持久化（通过 `*Dao` 类），例如：`users.csv`, `flights.csv`, `orders.csv`。这些 CSV文件通常存放在项目运行目录下或由 DAO 在第一次写入时创建。

-
# airplane-ticket-system
A java-based airline ticket booking management system
 631c5efb84c29869029db0b2939791062b8d7424
