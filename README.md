# Banking Management System

A comprehensive Java-based banking management system with advanced features including user role management, transaction processing, approval workflows, and modern UI/UX enhancements. This professional-grade application implements object-oriented programming principles with proper inheritance hierarchy, design patterns, and security features suitable for educational purposes, demonstration, and small to medium-scale banking operations.

![Java](https://img.shields.io/badge/Java-11+-orange?style=flat-square&logo=java)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-blue?style=flat-square)
![Maven](https://img.shields.io/badge/Build-Maven-red?style=flat-square&logo=apache-maven)
![License](https://img.shields.io/badge/License-Proprietary-yellow?style=flat-square)
![Version](https://img.shields.io/badge/Version-2.0.0-green?style=flat-square)

## 📋 Table of Contents

- [🎯 Project Overview](#-project-overview)
- [🚀 Features](#-features)
- [📁 Project Structure](#-project-structure)
- [🛠 System Requirements](#-system-requirements)
- [📦 Installation](#-installation)
- [🔐 Login Credentials](#-login-credentials)
- [📖 Usage Guide](#-usage-guide)
- [🔧 Configuration](#-configuration)
- [🔒 Security Features](#-security-features)
- [📊 Technical Details](#-technical-details)
- [🐛 Troubleshooting](#-troubleshooting)
- [📝 Version History](#-version-history)
- [🤝 Contributing](#-contributing)
- [🆘 Support](#-support)
- [📄 License](#-license)

## 🎯 Project Overview

The Banking Management System is a complete banking solution built with Java Swing that provides comprehensive functionality for modern banking operations. The system emphasizes user experience, security, and maintainability while offering a professional graphical user interface with role-based authentication, transaction processing, and comprehensive reporting capabilities.

### 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/fraol163/Banking-Management-System.git
cd Banking-Management-System

# Compile and run (Windows)
compile.bat
run-with-sample-data.bat

# Login with default credentials
# Username: admin, Password: password
```

### 🎯 Key Highlights

- 🏦 **Complete Banking Solution** - Account management, transactions, and reporting
- 👥 **Role-Based Access** - Admin, Manager, and Teller roles with specific permissions
- 🎨 **Modern UI** - Dark/Light themes with professional Java Swing interface
- 🔒 **Security First** - Input validation, encryption, and audit trails
- 📊 **Comprehensive Reports** - Transaction analysis and account summaries
- 🔄 **Approval Workflows** - Multi-level approval system for large transactions



## 🚀 Features

### Core Banking Operations
- **Account Management**: Create, view, and manage customer accounts (Savings, Checking, Business)
- **Customer Management**: Complete customer information management with validation
- **Transaction Processing**: Deposits, withdrawals, and transfers with real-time validation
- **Transaction History**: Comprehensive transaction tracking and reporting
- **Balance Management**: Real-time balance updates and validation

### Advanced Features
- **User Role Management**: Admin, Manager, and Teller roles with specific permissions
- **Approval Workflow**: Automatic approval routing for transactions exceeding limits
- **Dark/Light Theme Support**: Modern UI with theme persistence across sessions
- **Enhanced Error Handling**: User-friendly error messages with actionable guidance
- **Real-time Validation**: Input validation with immediate feedback
- **Database Backup/Restore**: Complete data export and import functionality
- **Comprehensive Help System**: Built-in documentation and user guides

### User Interface Enhancements
- **Account Selection Dropdowns**: Visual account selection across all transaction types
- **Transaction Filtering**: Real-time filtering by account selection
- **Responsive Design**: Modern, intuitive interface with consistent styling
- **Status Notifications**: Clear feedback for all user actions
- **Theme Switching**: Instant theme changes without application restart

## 🛠 System Requirements

### Software Requirements
- **Java**: JDK 11 or higher
- **Operating System**: Windows, macOS, or Linux
- **Memory**: Minimum 512MB RAM (1GB recommended)
- **Storage**: 100MB free disk space

### Dependencies
- **Swing**: For GUI components (included in JDK)
- **Java SQL**: For database operations (included in JDK)
- **Java Logging**: For system logging (included in JDK)
- **Java Crypto**: For encryption utilities (included in JDK)

## 📦 Installation

### Prerequisites
- **Java Development Kit (JDK) 11 or higher** installed and configured
- **JAVA_HOME environment variable** set correctly
- **Command prompt or terminal** access
- **Minimum 512MB RAM** (1GB recommended)
- **100MB free disk space** for application and data

### Quick Start Installation
1. **Download**: Clone or download the project files to your local machine
2. **Verify Java**: Ensure JDK 11+ is installed by running `java -version`
3. **Compile**: Run `compile.bat` (Windows) or compile manually using provided commands
4. **Run**: Execute one of the provided batch files based on your needs:
   - `run-banking-system.bat` - Start with system users only (minimal data)
   - `run-with-sample-data.bat` - Start with sample data for testing
   - `run-empty.bat` - Start with completely empty database
   - `run-reset.bat` - Reset database and start fresh
5. **Login**: Use default credentials from the Login Credentials section

### Manual Installation (Windows)
```batch
# Navigate to project directory
cd Banking-Management-System

# Create target directory if it doesn't exist
if not exist target\classes mkdir target\classes

# Compile in dependency order
javac -cp . -d target\classes src\main\java\com\bankingsystem\config\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\exceptions\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\models\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\utils\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\dao\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\services\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\security\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\gui\*.java
javac -cp target\classes -d target\classes src\main\java\com\bankingsystem\main\*.java

# Run the application
java -cp target\classes com.bankingsystem.main.BankingSystemApp
```

### Manual Installation (Linux/macOS)
```bash
# Clone the repository
git clone [repository-url]
cd Banking-Management-System

# Create target directory
mkdir -p target/classes

# Compile the project with proper classpath
javac -cp "lib/*" -d target/classes src/main/java/com/bankingsystem/**/*.java

# Run the application
java -cp "target/classes:lib/*" com.bankingsystem.main.BankingSystemApp
```

### Maven Installation (Alternative)
```bash
# If you prefer using Maven
mvn clean compile

# Run with Maven
mvn exec:java -Dexec.mainClass="com.bankingsystem.main.BankingSystemApp"

# Or build JAR and run
mvn package
java -jar target/banking-management-system-1.0.0-jar-with-dependencies.jar
```

### Installation Verification
After installation, verify the system is working correctly:
1. **Application Startup**: The login window should appear without errors
2. **Login Test**: Use admin/password to log in successfully
3. **Sample Data**: Load sample data via Tools → Load Sample Data
4. **Basic Operations**: Create a test account and perform a simple transaction
5. **Theme Test**: Switch between light and dark themes in Settings

## 📁 Project Structure

### Complete Directory Structure
```
Banking-Management-System/
├── src/main/java/com/bankingsystem/           # Source code root
│   ├── config/                                # Configuration management
│   │   ├── AppConfig.java                     # Application configuration settings
│   │   └── DatabaseConfig.java               # Database connection configuration
│   ├── dao/                                   # Data Access Objects (Database layer)
│   │   ├── AccountDAO.java                    # Account data operations
│   │   ├── CustomerDAO.java                   # Customer data operations
│   │   ├── ReportDAO.java                     # Report data operations
│   │   ├── TransactionDAO.java                # Transaction data operations
│   │   └── UserDAO.java                       # User data operations
│   ├── exceptions/                            # Custom exception classes
│   │   ├── DatabaseConnectionException.java   # Database connectivity errors
│   │   ├── InsufficientFundsException.java   # Insufficient balance errors
│   │   ├── InvalidAccountException.java       # Invalid account errors
│   │   ├── TransactionLimitExceededException.java # Transaction limit errors
│   │   └── TransferLimitExceededException.java # Transfer limit errors
│   ├── gui/                                   # User interface components
│   │   ├── AccountDetailsDialog.java          # Account information display
│   │   ├── AccountPanel.java                  # Account management interface
│   │   ├── AccountTransferDialog.java         # Account transfer interface
│   │   ├── ApprovalDialog.java                # Transaction approval interface
│   │   ├── BulkDeleteAccountsDialog.java      # Bulk account deletion
│   │   ├── CreateAccountDialog.java           # New account creation
│   │   ├── CreateCustomerDialog.java          # New customer creation
│   │   ├── CriteriaDeleteAccountsDialog.java  # Criteria-based deletion
│   │   ├── CustomerPanel.java                 # Customer management interface
│   │   ├── DashboardFrame.java                # Main application window
│   │   ├── DepositDialog.java                 # Deposit transaction interface
│   │   ├── EditUserDialog.java                # User editing interface
│   │   ├── LoginFrame.java                    # User authentication interface
│   │   ├── ReportsPanel.java                  # Reporting interface
│   │   ├── SettingsDialog.java                # Application settings
│   │   ├── SystemAdminPanel.java              # System administration
│   │   ├── TransactionHistoryDialog.java      # Transaction history display
│   │   ├── TransactionPanel.java              # Transaction management
│   │   ├── TransferDialog.java                # Transfer transaction interface
│   │   └── WithdrawDialog.java                # Withdrawal transaction interface
│   ├── main/                                  # Application entry point
│   │   └── BankingSystemApp.java              # Main application class
│   ├── models/                                # Data models and entities
│   │   ├── AbstractAccount.java               # Base account class
│   │   ├── AbstractTransaction.java           # Base transaction class
│   │   ├── AbstractUser.java                  # Base user class
│   │   ├── AccountSummaryReport.java          # Account summary data model
│   │   ├── BusinessAccount.java               # Business account implementation
│   │   ├── CheckingAccount.java               # Checking account implementation
│   │   ├── Customer.java                      # Customer data model
│   │   ├── SavingsAccount.java                # Savings account implementation
│   │   ├── Transaction.java                   # Transaction data model
│   │   ├── TransactionAnalysisReport.java     # Transaction analysis data
│   │   ├── TransactionApproval.java           # Approval workflow data
│   │   ├── TransferRequest.java               # Transfer request data
│   │   ├── TransferResult.java                # Transfer result data
│   │   ├── User.java                          # User data model
│   │   └── UserActivityReport.java            # User activity data
│   ├── security/                              # Security and authentication
│   │   ├── LoginAttemptTracker.java           # Login attempt monitoring
│   │   ├── PasswordPolicy.java                # Password policy enforcement
│   │   ├── PasswordStrengthLevel.java         # Password strength enumeration
│   │   ├── PasswordValidationResult.java      # Password validation results
│   │   └── PasswordValidator.java             # Password validation logic
│   ├── services/                              # Business logic layer
│   │   ├── AccountService.java                # Account business operations
│   │   ├── ApprovalService.java               # Approval workflow management
│   │   ├── CustomerService.java               # Customer business operations
│   │   ├── ReportService.java                 # Report generation services
│   │   ├── TransactionService.java            # Transaction business logic
│   │   ├── TransferService.java               # Transfer operations
│   │   └── UserService.java                   # User management services
│   └── utils/                                 # Utility classes and helpers
│       ├── ApprovalWorkflowManager.java       # Approval workflow utilities
│       ├── DatabaseBackupUtil.java            # Database backup operations
│       ├── DatabaseUtil.java                  # Database utility functions
│       ├── EncryptionUtil.java                # Encryption and security utilities
│       ├── ErrorHandler.java                  # Error handling utilities
│       ├── FileUtil.java                      # File operation utilities
│       ├── MockDatabaseUtil.java              # Mock database implementation
│       ├── ReportDataValidator.java           # Report data validation
│       ├── SettingsManager.java               # Application settings management
│       ├── ThemeManager.java                  # UI theme management
│       ├── UserFriendlyValidation.java        # User-friendly validation
│       └── ValidationUtil.java                # General validation utilities
├── src/main/resources/                        # Resource files
├── target/classes/                            # Compiled Java classes
│   └── com/                                   # Compiled package structure
├── lib/                                       # External libraries
│   └── sqlite-jdbc-3.42.0.0.jar             # SQLite JDBC driver
├── backups/                                   # Database backup storage
├── exports/                                   # Data export storage
├── imports/                                   # Data import storage
├── reports/                                   # Generated report storage
├── banking_system_settings.properties        # Application settings file
├── compile.bat                                # Windows compilation script
├── run-banking-system.bat                     # Windows run script (system users only)
├── run-with-sample-data.bat                   # Windows run script (with sample data)
├── run-empty.bat                              # Windows run script (empty database)
├── run-reset.bat                              # Windows run script (reset database)
├── pom.xml                                    # Maven project configuration
├── PROJECT_SUMMARY.md                         # Markdown project summary
├── PROJECT_SUMMARY.txt                        # Text project summary
├── README.md                                  # This documentation file
└── EnhancedAuthenticationVerificationTest.java # Authentication testing
```

### Key Components Description

#### Configuration Layer (`config/`)
- **AppConfig.java**: Manages application-wide configuration settings, default values, and system parameters
- **DatabaseConfig.java**: Handles database connection configuration, connection pooling, and database-specific settings

#### Data Access Layer (`dao/`)
- **AccountDAO.java**: Provides CRUD operations for account data, account queries, and account-related database operations
- **CustomerDAO.java**: Manages customer data persistence, customer queries, and customer relationship operations
- **ReportDAO.java**: Handles report data generation, aggregation queries, and report-specific database operations
- **TransactionDAO.java**: Manages transaction data persistence, transaction history, and transaction queries
- **UserDAO.java**: Provides user authentication data, user management operations, and user-related queries

#### Business Logic Layer (`services/`)
- **AccountService.java**: Implements account business rules, account validation, and account management workflows
- **ApprovalService.java**: Manages approval workflows, approval routing, and approval status tracking
- **CustomerService.java**: Implements customer business logic, customer validation, and customer management
- **ReportService.java**: Provides report generation, data analysis, and report formatting services
- **TransactionService.java**: Implements transaction processing, validation, and business rule enforcement
- **TransferService.java**: Manages transfer operations, transfer validation, and transfer workflow coordination
- **UserService.java**: Provides user management, authentication, and user role management services

#### User Interface Layer (`gui/`)
- **DashboardFrame.java**: Main application window with navigation, menu system, and overall application layout
- **LoginFrame.java**: User authentication interface with login validation and session initiation
- **AccountPanel.java**: Account management interface with account listing, creation, and management features
- **TransactionPanel.java**: Transaction processing interface with deposit, withdrawal, and transfer capabilities
- **ReportsPanel.java**: Reporting interface with report generation, filtering, and export capabilities

#### Security Layer (`security/`)
- **PasswordValidator.java**: Implements password strength validation and password policy enforcement
- **LoginAttemptTracker.java**: Monitors and tracks login attempts for security purposes
- **PasswordPolicy.java**: Defines and enforces password policies and security requirements

#### Utility Layer (`utils/`)
- **ThemeManager.java**: Manages UI themes, theme switching, and theme persistence
- **SettingsManager.java**: Handles application settings, user preferences, and configuration persistence
- **DatabaseBackupUtil.java**: Provides database backup, restore, and data export/import functionality
- **ValidationUtil.java**: Implements comprehensive input validation and data validation utilities

## 🔐 Login Credentials

### Default User Accounts
| Role | Username | Password | Permissions |
|------|----------|----------|-------------|
| **Admin** | admin | password | Full system access, unlimited transactions |
| **Manager** | manager | password | Approve transactions, $10,000 daily limit |
| **Teller** | teller | password | Basic transactions, $2,000 daily limit |

### Permission Levels
- **Admin**: All features, no approval required, unlimited transaction amounts
- **Manager**: Transaction approval, self-approval up to $10,000, user management
- **Teller**: Basic transactions, approval required for amounts > $1,000

## 📖 Usage Guide

### Getting Started
1. **Launch Application**: Run the banking system executable
2. **Login**: Enter username and password from the credentials table
3. **Load Sample Data**: Go to Tools → Load Sample Data (recommended for first use)
4. **Explore Features**: Navigate through different panels and features

### Core Workflows

#### Creating Accounts
1. Navigate to **Account Management** panel
2. Click **Create Account** button
3. Select customer from dropdown or create new customer
4. Choose account type (Savings, Checking, Business)
5. Enter initial deposit (minimum $100.00, maximum $50,000.00)
6. Click **Create Account**

#### Processing Transactions
1. Go to **Transaction Management** panel
2. Select transaction type: **Deposit**, **Withdraw**, or **Transfer**
3. Choose account from dropdown (shows: Account Number - Customer Name - Type - Balance)
4. Enter amount and description
5. Click process button
6. System will either process immediately or create approval request

#### Managing Approvals (Manager/Admin)
1. Click **Approvals** button in main interface
2. Review pending approval requests
3. Select request and add comments
4. Click **Approve** or **Reject**
5. Requesting user will be notified of decision

#### Using Reports
1. Navigate to **Reports** panel
2. Select report type and date range
3. Apply filters as needed
4. View results in table format
5. Export data if required

### Advanced Features

#### Theme Switching
1. Go to **Tools** → **Settings**
2. Select **Appearance** tab
3. Choose **Light** or **Dark** theme
4. Theme applies immediately
5. Setting persists across application restarts

#### Database Backup
1. Go to **Tools** → **Export Database Backup**
2. Choose save location and filename
3. System exports all data to .bms file
4. Use **Tools** → **Backup Information** for details

#### Account Filtering
1. In **Transaction Management** panel
2. Use **Filter by Account** dropdown in header
3. Select specific account or "All Accounts"
4. Transaction table updates automatically
5. Use **Refresh Accounts** to reload account list

## 🔧 Configuration

### Settings Management
Access via **Tools** → **Settings**:

- **Appearance**: Theme selection, look and feel options
- **Behavior**: Session timeout, auto-save preferences
- **Formats**: Date and currency display formats
- **Performance**: Transaction history limits, cache settings

### User Permissions
Configured in user management (Admin only):
- Transaction limits by role
- Feature access permissions
- Approval thresholds
- Daily transaction limits

## 🐛 Troubleshooting

### Common Issues

#### Application Won't Start
- **Check Java Version**: Ensure JDK 11+ is installed
- **Verify Classpath**: Ensure all required files are present
- **Check Permissions**: Verify read/write access to application directory

#### Login Problems
- **Default Credentials**: Use admin/password for initial access
- **Case Sensitivity**: Usernames and passwords are case-sensitive
- **Database Issues**: Try restarting application or loading sample data

#### Transaction Errors
- **Amount Limits**: Check transaction limits for your user role
- **Account Selection**: Ensure valid account is selected from dropdown
- **Insufficient Funds**: Verify account balance before withdrawal/transfer
- **Approval Required**: Large transactions may need manager approval

#### UI Issues
- **Theme Problems**: Reset to default theme in Settings
- **Display Issues**: Try different look and feel options
- **Responsiveness**: Ensure adequate system memory

### Error Messages
The system provides user-friendly error messages with specific guidance:
- **Validation Errors**: Clear input requirements and limits
- **Permission Errors**: Explanation of required permissions
- **Transaction Errors**: Specific reasons and suggested actions
- **System Errors**: Troubleshooting steps and contact information

### Getting Help
- **Built-in Help**: Use **Help** menu for comprehensive documentation
- **Quick Help**: Check sidebar help buttons for context-sensitive assistance
- **Settings Help**: Each settings tab includes detailed explanations
- **Error Dialogs**: Follow suggested troubleshooting steps

## 🔒 Security Features

### Data Protection
- **Input Validation**: Comprehensive validation prevents invalid data entry
- **SQL Injection Prevention**: Parameterized queries protect against attacks
- **Role-based Access**: Users only access features appropriate to their role
- **Session Management**: Automatic logout after inactivity

### Transaction Security
- **Approval Workflows**: Large transactions require manager approval
- **Audit Trail**: Complete transaction history with timestamps
- **Reference Numbers**: Unique identifiers for all transactions
- **Balance Validation**: Real-time balance checks prevent overdrafts

## 📊 Technical Details

### System Architecture

#### Layered Architecture Design
The Banking Management System follows a well-structured layered architecture pattern:

1. **Presentation Layer** (`gui/`)
   - Java Swing-based user interface components
   - MVC pattern implementation for UI components
   - Event-driven programming model
   - Theme management and responsive design

2. **Business Logic Layer** (`services/`)
   - Core business rule implementation
   - Transaction processing logic
   - Approval workflow management
   - Data validation and business constraints

3. **Data Access Layer** (`dao/`)
   - Database abstraction and CRUD operations
   - Query optimization and data mapping
   - Transaction management and rollback support
   - Mock database implementation for demonstration

4. **Model Layer** (`models/`)
   - Domain object definitions
   - Abstract base classes with inheritance hierarchy
   - Data transfer objects and value objects
   - Business entity relationships

5. **Utility Layer** (`utils/`)
   - Cross-cutting concerns and helper functions
   - Configuration management
   - Security utilities and encryption
   - File operations and data export/import

#### Design Patterns Implementation

**Creational Patterns:**
- **Factory Pattern**: Account creation with different types (Savings, Checking, Business)
- **Singleton Pattern**: Configuration management and settings persistence
- **Builder Pattern**: Complex object construction for reports and transactions

**Structural Patterns:**
- **Adapter Pattern**: Database abstraction layer for different data sources
- **Facade Pattern**: Service layer providing simplified interfaces to complex subsystems
- **Decorator Pattern**: Enhanced validation and error handling

**Behavioral Patterns:**
- **Observer Pattern**: Real-time UI updates and event notification system
- **Strategy Pattern**: Different transaction processing strategies and validation rules
- **Command Pattern**: Action handling and undo/redo functionality
- **State Pattern**: Account status management and transaction state handling

### Technology Stack

#### Core Technologies
- **Programming Language**: Java 11+ with modern language features
- **User Interface**: Java Swing with custom look-and-feel implementations
- **Database**: In-memory database with SQLite JDBC driver for persistence
- **Build System**: Maven for dependency management and build automation
- **Testing**: JUnit for unit testing and integration testing

#### Key Libraries and Dependencies
- **SQLite JDBC Driver (3.42.0.0)**: Database connectivity and SQL operations
- **HikariCP (5.0.1)**: High-performance JDBC connection pooling
- **Java Swing**: Native GUI framework with cross-platform compatibility
- **Java Security**: Built-in encryption and security utilities

#### Development Tools
- **Maven**: Project management and build automation
- **Batch Scripts**: Windows-specific compilation and execution scripts
- **Properties Files**: Configuration management and settings persistence

### Database Design

#### In-Memory Database Implementation
- **Thread-safe operations**: Concurrent access handling with proper synchronization
- **ACID compliance**: Transaction integrity and rollback support
- **Data persistence**: File-based storage for data durability
- **Query optimization**: Efficient data retrieval and indexing

#### Data Model Structure
- **Users**: Authentication and role-based access control
- **Customers**: Personal and business customer information
- **Accounts**: Multiple account types with inheritance hierarchy
- **Transactions**: Complete transaction history with audit trail
- **Approvals**: Workflow management and approval tracking

### Performance Optimizations

#### Memory Management
- **Lazy Loading**: Data loaded on demand to minimize memory usage
- **Object Pooling**: Reuse of expensive objects like database connections
- **Garbage Collection**: Efficient memory cleanup and resource management
- **Caching Strategy**: Frequently accessed data cached for improved performance

#### Database Optimizations
- **Connection Pooling**: Efficient database connection management
- **Prepared Statements**: SQL injection prevention and query optimization
- **Batch Operations**: Bulk data operations for improved throughput
- **Index Usage**: Strategic indexing for fast data retrieval

#### UI Performance
- **Background Processing**: Non-blocking UI operations using SwingWorker
- **Event Dispatch Thread**: Proper UI thread management
- **Component Optimization**: Efficient rendering and update strategies
- **Theme Caching**: Pre-loaded themes for instant switching

### Security Architecture

#### Authentication and Authorization
- **Role-based Access Control (RBAC)**: Granular permission management
- **Session Management**: Secure session handling with timeout
- **Password Security**: Hashing and encryption for password storage
- **Login Attempt Tracking**: Brute force attack prevention

#### Data Protection
- **Input Validation**: Comprehensive validation to prevent injection attacks
- **Data Encryption**: Sensitive data encryption using industry standards
- **Audit Logging**: Complete audit trail for all system operations
- **Error Handling**: Secure error messages without information disclosure

#### Transaction Security
- **Approval Workflows**: Multi-level approval for high-value transactions
- **Transaction Limits**: Role-based transaction amount restrictions
- **Reference Numbers**: Unique transaction identifiers for tracking
- **Balance Validation**: Real-time balance checks to prevent overdrafts

## 📝 Version History

### Current Version: 2.0.0
- Enhanced error handling with user-friendly messages
- Comprehensive approval workflow system
- Dark/light theme support with persistence
- Account selection dropdowns for all transaction types
- Real-time input validation and feedback
- Improved transaction management with filtering
- Complete help system and documentation

### Previous Versions
- **1.5.0**: Settings management and database backup
- **1.0.0**: Core banking functionality and user management

## 🤝 Contributing

We welcome contributions to the Banking Management System! Here's how you can help:

### 🐛 Reporting Issues
- Use the [GitHub Issues](../../issues) page to report bugs
- Provide detailed information about the issue
- Include steps to reproduce the problem
- Attach screenshots if applicable

### 💡 Suggesting Enhancements
- Open a [Feature Request](../../issues/new) on GitHub
- Describe the enhancement in detail
- Explain the use case and benefits
- Consider implementation complexity

### 🔧 Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### 📝 Code Guidelines
- Follow Java coding conventions
- Add appropriate comments and documentation
- Include unit tests for new features
- Ensure all tests pass before submitting
- Update documentation as needed

## 🆘 Support

### Getting Help
For technical support or questions, follow this escalation path:

1. **Built-in Help System**: Use the comprehensive Help menu for detailed documentation
2. **Context-Sensitive Help**: Check sidebar help buttons for feature-specific assistance
3. **Documentation Review**: Refer to this README and PROJECT_DOCUMENTATION.txt
4. **Troubleshooting Section**: Follow the detailed troubleshooting guide above
5. **Error Dialog Guidance**: Follow suggested steps in error messages
6. **System Administrator**: Contact your system administrator for advanced issues

### Additional Resources
- **PROJECT_SUMMARY.md**: Markdown format project summary with implementation details
- **PROJECT_SUMMARY.txt**: Text format comprehensive project overview
- **PROJECT_DOCUMENTATION.txt**: Complete plain text documentation with all details
- **Settings Help**: Each settings tab includes detailed explanations and guidance

### Support Channels
- **Built-in Documentation**: Complete user guides and help system
- **Error Handling**: User-friendly error messages with actionable guidance
- **Validation Feedback**: Real-time input validation with immediate feedback
- **System Monitoring**: Comprehensive logging for troubleshooting and analysis

## 📄 License

This Banking Management System is proprietary software. All rights reserved.

## 🎯 Project Summary

This Banking Management System represents a complete, professional-grade application that successfully implements all specified requirements including:

- **Object-Oriented Design**: Proper inheritance hierarchy with abstract base classes
- **Comprehensive Banking Features**: Complete account management, transaction processing, and reporting
- **Secure Authentication**: Role-based access control with session management
- **Professional GUI**: Modern Java Swing interface with theme support
- **Advanced Workflows**: Approval systems and transaction management
- **Data Management**: Backup/restore functionality and data export/import
- **Performance Optimization**: Efficient memory usage and database operations
- **Security Features**: Input validation, encryption, and audit trails

The system is ready for demonstration and can serve as a foundation for a production banking application with proper database integration and additional security measures.

---

**Banking Management System v2.0.0** - A comprehensive solution for modern banking operations with enhanced user experience, robust security features, and professional-grade functionality suitable for educational purposes, demonstration, and small to medium-scale banking operations.
