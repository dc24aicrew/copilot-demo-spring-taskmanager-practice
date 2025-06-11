# 🧪 Practice Guide for Copilot Demo

## 🎯 Quick Start for Practice

### Step 1: Copy Full Project from Original
```bash
# You need to copy the complete project structure from the original repository
# This practice repo currently only has the README and issues

# 1. Clone original repository
git clone https://github.com/dc24aicrew/copilot-demo-spring-taskmanager.git original

# 2. Copy all content to this practice repo
cp -r original/* .
cp original/.gitignore .
cp -r original/.github .

# 3. Commit everything
git add .
git commit -m "Copy complete project for practice"
git push origin main
```

### Step 2: Practice Copilot Assignment
1. **Start with Issue #2** (Security - safest for practice)
2. **Go to Issues tab** in GitHub
3. **Click on Issue #2**
4. **Assign to Copilot** (look for assignment option)
5. **Watch the process**
6. **Practice your demo talking points**

### Step 3: Reset if Needed
```bash
# If you want to start over
git reset --hard HEAD~1  # Removes last commit
git push --force origin main
```

## 🎪 Demo Practice Scenarios

### Quick Win (15 min)
- Issue #2: Security fixes
- Focus: Business value, immediate impact

### Architecture Deep Dive (30 min)  
- Issue #4: Clean Architecture (create this issue after copying project)
- Focus: Technical excellence, code quality

### Enterprise Showcase (45 min)
- Issue #1: Microservices migration
- Focus: Large-scale transformation

## 🚨 Important Notes

- **This is a PRACTICE repository** - safe to experiment
- **Real project files are NOT copied yet** - you need to do Step 1
- **Issues are ready** - perfect for practicing assignments
- **No impact on original** - your main demo repo stays clean

## ✅ Ready Status

- [x] Practice repository created
- [x] Demo issues created
- [ ] **Project files copied (YOU NEED TO DO THIS)**
- [ ] Environment tested
- [ ] Demo flow practiced

**Next:** Copy the project files from original repository to start practicing!
