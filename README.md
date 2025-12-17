Dorm Allocation System




A modern, feature-rich desktop application for managing student dormitory allocations with an elegant dark-themed UI featuring glassmorphism design patterns.



## 📸 Screenshots

### Landing Page

<img width="960" height="600" alt="Screenshot 2025-12-07 172702" src="https://github.com/user-attachments/assets/46d2200b-0fdb-4a7c-8453-3bd3cc1965d8" />


### Student Portal


<img width="960" height="600" alt="Screenshot 2025-12-07 172720" src="https://github.com/user-attachments/assets/72a7821a-6873-4f8a-8e07-869f82802384" />

#### Student Dashboard

<img width="960" height="600" alt="image" src="https://github.com/user-attachments/assets/d24074e6-bb92-42ec-ad03-e5d30f21ad14" />

### Proctor Portal

#### Proctor Login
<img width="959" height="600" alt="Screenshot 2025-12-07 172734" src="https://github.com/user-attachments/assets/c55097f7-66d4-4389-9c42-552a41feb17a" />


#### Proctor Dashboard

<img width="960" height="600" alt="image" src="https://github.com/user-attachments/assets/adfb8b7a-3f1a-48b2-9008-1c8607188349" />

<img width="960" height="600" alt="image" src="https://github.com/user-attachments/assets/65d28903-845f-47ab-ad1f-e6d9b34cdf6f" />




#### Student Management View

<img width="960" height="600" alt="Screenshot 2025-12-07 172816" src="https://github.com/user-attachments/assets/6e0c2560-f9e5-4dfe-8aad-5f29ba8eb5a9" />

---

## ✨ Features

### 🎓 Student Features
- **Secure Authentication**: Login and registration system with password protection
- **Personal Dashboard**: View profile information including name, ID, department, year, and contact details
- **Dorm Assignment Tracking**: Real-time view of assigned building and room number
- **Gender-Specific Allocations**: Automatic assignment to gender-appropriate dormitories

### 👨‍🏫 Proctor (Admin) Features
- **Comprehensive Dashboard**: 
  - Real-time statistics (total students, buildings, available rooms)
  - Visual building cards with gender indicators
  - Quick access to all management functions
  
- **Student Management**:
  - View all registered students in a searchable, sortable table
  - Filter by assignment status, gender, department, or year
  - Manual room assignment and reassignment
  - Auto-assignment algorithm for efficient bulk allocation
  - Export student data to CSV reports
  - Add, edit, and remove student records
  
- **Building Management**:
  - Create and configure dormitory buildings
  - Set gender-specific buildings (Male/Female)
  - Define room capacity and total rooms per building
  - Visual indicators for building occupancy
  - Delete buildings (with validation checks)
  
- **System Configuration**:
  - Configure default building settings
  - Manage system-wide preferences
  - Data persistence via CSV files

### 🎨 UI/UX Highlights
- **Modern Dark Theme**: Professional dark blue gradient background
- **Glassmorphism Effects**: Frosted glass aesthetic with subtle transparency
- **Smooth Animations**: Hover effects, transitions, and micro-interactions
- **Responsive Design**: Clean layouts with proper spacing and alignment
- **Color-Coded Elements**: Gender indicators (blue for male, pink for female)
- **Intuitive Navigation**: Clear role-based access and workflow

---

## 🛠️ Technology Stack

- **Language**: Java 23
- **UI Framework**: JavaFX 21
- **Architecture**: Model-View-Controller (MVC)
- **Data Persistence**: CSV files
- **Styling**: Custom CSS with modern design patterns
- **Build Tool**: Maven (optional) / Standard Java compilation

---

## 📋 Prerequisites

- **Java Development Kit (JDK)**: Version 23 or higher
- **JavaFX SDK**: Version 21 (if not using Maven)
- **IDE** (Optional but recommended): IntelliJ IDEA, Eclipse, or NetBeans

---

## 🚀 Installation & Setup

### Option 1: Using Maven (Recommended)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/dorm-allocation-system.git
   cd dorm-allocation-system
   ```

2. **Run the application**:
   ```bash
   mvn javafx:run
   ```

   > **Note**: If Maven is not in your system PATH, use the full path to Maven:
   ```bash
   "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.4.1\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
   ```

### Option 2: Manual Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/dorm-allocation-system.git
   cd dorm-allocation-system
   ```

2. **Compile the project**:
   ```bash
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -d bin src/**/*.java
   ```

3. **Run the application**:
   ```bash
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp bin Main
   ```

---

## 📖 Usage Guide

### First-Time Setup

1. **Launch the Application**: Run the application using one of the methods above
2. **Initial Proctor Login**: Use default credentials (configure in `proctors.csv`)
3. **Configure Buildings**: Navigate to Settings and create dormitory buildings
4. **Add Students**: Students can self-register or proctors can add them manually

### Student Workflow

1. **Register**: Click "Student Portal" → "Register" and fill in your details
2. **Login**: Use your student ID and password to access your dashboard
3. **View Assignment**: Check your assigned building and room number
4. **Update Profile**: Contact proctor for any changes needed

### Proctor Workflow

1. **Login**: Click "Proctor Portal" and enter admin credentials
2. **Dashboard Overview**: View system statistics and building status
3. **Manage Buildings**:
   - Click "Settings" to add new buildings
   - Specify building name, gender, total rooms, and capacity per room
4. **Manage Students**:
   - Navigate to "Students" tab to view all registered students
   - Use filters to find specific students
   - Assign rooms manually or use "Auto-Assign" for bulk allocation
5. **Export Reports**: Click "Export CSV" to generate student reports

---

## 📁 Project Structure

```
Dorm-Allocation-project-2/
├── src/
│   ├── Main.java                          # Application entry point
│   ├── controllers/                       # MVC Controllers
│   │   ├── LandingController.java        # Landing page logic
│   │   ├── StudentAuthController.java    # Student login/register
│   │   ├── StudentDashboardController.java
│   │   ├── ProctorLoginController.java
│   │   ├── ProctorDashboardController.java
│   │   ├── ProctorSettingsController.java
│   │   ├── ProctorStudentsController.java
│   │   └── SessionManager.java           # Session management
│   ├── models/                           # Data models
│   │   ├── Student.java
│   │   ├── Proctor.java
│   │   ├── Building.java
│   │   └── Room.java
│   ├── data/                             # Data management
│   │   └── DataManager.java             # CSV operations & data handling
│   ├── utils/                            # Utility classes
│   │   └── NavigationUtils.java         # Scene navigation helper
│   └── resources/                        # FXML and CSS files
│       ├── LandingPage.fxml
│       ├── StudentAuth.fxml
│       ├── StudentDashboard.fxml
│       ├── ProctorLogin.fxml
│       ├── ProctorDashboard.fxml
│       ├── ProctorSettings.fxml
│       ├── ProctorStudentsView.fxml
│       └── style.css                    # Modern dark theme styling
├── students.csv                          # Student data storage
├── proctors.csv                         # Proctor credentials
├── config.csv                           # System configuration
└── README.md
```

---

## 🗄️ Data Storage

The application uses CSV files for data persistence:

- **students.csv**: Stores student information (name, ID, password, department, year, gender, assignments)
- **proctors.csv**: Stores proctor credentials (ID, password)
- **config.csv**: Stores building configurations (name, gender, rooms, capacity)

> **Note**: All data files are created automatically on first run if they don't exist.

---

## 🎨 Design Philosophy

DormFlow is built with a focus on:

- **Modern Aesthetics**: Dark theme with cyan accents (#00b4d8) for a professional look
- **User Experience**: Intuitive navigation and clear visual hierarchy
- **Glassmorphism**: Frosted glass effects for depth and elegance
- **Accessibility**: High contrast text and clear visual indicators
- **Responsiveness**: Smooth animations and hover effects for better interaction feedback

---

## 🔒 Security Features

- Password-protected authentication for both students and proctors
- Role-based access control (Student vs. Proctor permissions)
- Session management to maintain user state
- Data validation for all inputs

---

## 🚧 Future Enhancements

- [ ] Database integration (MySQL/PostgreSQL) for scalability
- [ ] Email notifications for room assignments
- [ ] Advanced reporting and analytics
- [ ] Mobile application companion
- [ ] Multi-language support
- [ ] Dark/Light theme toggle
- [ ] Room swap requests between students
- [ ] Maintenance request system

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

