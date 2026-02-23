# Managing Large APK Files with Git LFS (Optional)

## Current Status

The APK has been successfully pushed to GitHub, but you received a warning:

```
warning: File releases/readllm-v1.0-release.apk is 91.64 MB; 
this is larger than GitHub's recommended maximum file size of 50.00 MB
```

## Options

### Option 1: Keep as is (Current)
- ✅ APK is in the repo and accessible
- ⚠️ Repo size will grow with each release
- ⚠️ Cloning the repo will be slower

### Option 2: Use Git LFS (Recommended for future releases)

Git LFS (Large File Storage) is designed for large binary files.

#### Setup Git LFS

1. **Install Git LFS:**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install git-lfs
   
   # macOS
   brew install git-lfs
   ```

2. **Initialize Git LFS in your repo:**
   ```bash
   cd /home/diego/Development/archive/readllm
   git lfs install
   ```

3. **Track APK files:**
   ```bash
   git lfs track "*.apk"
   git add .gitattributes
   git commit -m "chore: Track APK files with Git LFS"
   ```

4. **Future APK files will automatically use LFS**

#### Migrate Existing APK to LFS (Optional)

If you want to convert the current APK to LFS:

```bash
# Install BFG Repo Cleaner
# Download from: https://rtyley.github.io/bfg-repo-cleaner/

# Convert to LFS
git lfs migrate import --include="*.apk" --everything

# Force push (CAUTION: rewrites history)
git push --force origin main
```

⚠️ **Warning:** This rewrites git history. Only do this if you're the only contributor.

### Option 3: Use GitHub Releases (Recommended)

Instead of committing APKs to the repo, use GitHub Releases:

1. **Go to your repo on GitHub**
2. **Click "Releases" → "Create a new release"**
3. **Tag:** v1.0
4. **Title:** ReadLLM v1.0
5. **Description:** Copy from releases/README.md
6. **Upload APK as a release asset**

**Benefits:**
- ✅ APKs don't bloat the repo
- ✅ Easy to download specific versions
- ✅ Automatic release notes
- ✅ Can have multiple APKs per release (debug, release, etc.)

#### Creating a Release via GitHub CLI

```bash
# Install gh CLI
sudo apt-get install gh  # Ubuntu/Debian

# Authenticate
gh auth login

# Create release
gh release create v1.0 \
  --title "ReadLLM v1.0" \
  --notes-file releases/README.md \
  releases/readllm-v1.0-release.apk
```

## Recommendation

**For this release:** Keep as is (already pushed)

**For future releases:** Use GitHub Releases feature instead of committing APKs

## Future Workflow

```bash
# 1. Build APK
./gradlew assembleRelease

# 2. Copy to releases (for documentation)
cp app/build/outputs/apk/release/app-release-unsigned.apk \
   releases/readllm-v1.1-release.apk

# 3. Create GitHub release (not committed to repo)
gh release create v1.1 \
  --title "ReadLLM v1.1" \
  --notes "Release notes here" \
  releases/readllm-v1.1-release.apk

# 4. Remove from releases folder (keep repo clean)
git rm releases/readllm-v1.0-release.apk
git commit -m "chore: Remove APK (moved to GitHub releases)"
```

---

**Current Status: ✅ All changes pushed successfully to GitHub!**

The APK is available at:
`https://github.com/alektebel/readllm/blob/main/releases/readllm-v1.0-release.apk`
