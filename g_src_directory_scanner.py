"""Directory structure analyzer using established core utilities"""
import os
from typing import Dict, Any, List
import json
from datetime import datetime
# Import using a proper relative import syntax
import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Configuration options
# Set AS_IS_MODE to True to preserve the current directory structure without sorting
AS_IS_MODE = False
# Set SORT_BY_FILE_COUNT to True to sort directories by the number of files (largest first)
SORT_BY_FILE_COUNT = True

def analyze_directory_structure(base_path: str, as_is_mode: bool = False, sort_by_file_count: bool = False) -> Dict[str, Any]:
    # Get the actual directory name
    dir_name = os.path.basename(base_path)

    # Initialize structure with metadata
    structure = {
        "project_name": dir_name,
        "analysis_date": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "version": 9,
        "analysis_mode": "as_is" if as_is_mode else ("sorted_by_file_count" if sort_by_file_count else "default"),
        # Use the actual directory name as the key for the directory tree
        dir_name: {}
    }

    # Build hierarchical directory structure
    def build_directory_tree(path, rel_path="."):
        # Get files in current directory
        files = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
        js_files = [f for f in files if f.endswith('.js')]

        # Create directory node with subdirectories first (prioritizing subdirectories over files)
        dir_node = {
            "subdirectories": {},
            "files": files
        }

        # Process subdirectories
        subdirs = []
        for item in os.listdir(path):
            item_path = os.path.join(path, item)
            if os.path.isdir(item_path):
                # Collect subdirectories for potential sorting
                subdirs.append((item, item_path, os.path.join(rel_path, item)))

        # Sort subdirectories if requested
        if sort_by_file_count and not as_is_mode:
            # Count files in each subdirectory (including nested files)
            def count_all_files(dir_path):
                total = 0
                for root, _, files in os.walk(dir_path):
                    total += len(files)
                return total

            # Sort by file count (descending)
            subdirs.sort(key=lambda x: count_all_files(x[1]), reverse=True)

        # Process subdirectories (sorted or as-is)
        for item, item_path, item_rel_path in subdirs:
            # Recursively build tree for subdirectory
            dir_node["subdirectories"][item] = build_directory_tree(item_path, item_rel_path)

        return dir_node

    # Build the tree starting from base_path
    structure[dir_name] = build_directory_tree(base_path)

    # Add file details by extension, organized in a hierarchical structure
    structure["files_by_extension"] = {}

    # Helper function to build a path tree
    def build_path_tree(path_parts, file_info, current_level):
        if not path_parts:
            if "files" not in current_level:
                current_level["files"] = []
            current_level["files"].append(file_info)
            return

        part = path_parts[0]
        if "subdirectories" not in current_level:
            current_level["subdirectories"] = {}

        if part not in current_level["subdirectories"]:
            current_level["subdirectories"][part] = {}

        build_path_tree(path_parts[1:], file_info, current_level["subdirectories"][part])

    # Process each extension type
    for ext in ['.js', '.css', '.jsx']:
        ext_key = ext.lstrip('.')
        structure["files_by_extension"][ext_key] = {"subdirectories": {}}

        # Find all files with this extension
        for root, _, files in os.walk(base_path):
            for file in files:
                if file.endswith(ext):
                    # Get relative path and split into parts
                    rel_path = os.path.relpath(root, base_path)
                    path_parts = [] if rel_path == "." else rel_path.split(os.sep)

                    # Create file info
                    file_info = {
                        "name": file,
                        "path": os.path.join(rel_path, file),
                        "size": os.path.getsize(os.path.join(root, file))
                    }

                    # Add to the tree
                    build_path_tree(path_parts, file_info, structure["files_by_extension"][ext_key])

    return structure

def save_structure(structure: Dict[str, Any], output_file: str) -> None:
    with open(output_file, 'w') as f:
        json.dump(structure, f, indent=2)

def main():
    # Use relative paths
    current_dir = os.path.dirname(__file__)
    output_file = os.path.join(current_dir, "g_src_directory_scanner.json")
    Base_dir = os.path.join(current_dir, "src")

    if not os.path.exists(Base_dir):
        print(f"Directory not found: {Base_dir}")
        return

    # Use the configuration options defined at the top of the file
    structure = analyze_directory_structure(Base_dir, as_is_mode=AS_IS_MODE, sort_by_file_count=SORT_BY_FILE_COUNT)
    save_structure(structure, output_file)

    # Print analysis mode
    analysis_mode = structure.get("analysis_mode", "default")
    print(f"Analysis mode: {analysis_mode}")
    print(f"Directory structure analysis saved to: {output_file}")

    # Summary statistics
    def count_directories(node):
        if not node or "subdirectories" not in node:
            return 0
        return len(node["subdirectories"]) + sum(count_directories(subdir) for subdir in node["subdirectories"].values())

    def count_files(node):
        if not node:
            return 0
        file_count = len(node.get("files", []))
        if "subdirectories" in node:
            file_count += sum(count_files(subdir) for subdir in node["subdirectories"].values())
        return file_count

    # Get the directory name from the structure
    dir_name = structure["project_name"]
    total_dirs = count_directories(structure[dir_name])
    total_files = count_files(structure[dir_name])

    print(f"\nAnalysis Summary:")
    print(f"Total directories: {total_dirs}")
    print(f"Total files: {total_files}")
    print(f"File types found: {', '.join(structure['files_by_extension'].keys())}")

if __name__ == "__main__":
    main()
