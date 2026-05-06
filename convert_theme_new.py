import os
import re

color_map = {
    # Backgrounds
    '#fcfaf6': '#F3E3D0',
    '#f5f2eb': '#EAE0D2', # slightly darker for sidebar
    '#ffffff': '#FCF9F5', # very light for cards
    '#e6e2d8': '#D2C4B4', # border
    '#c9c3b8': '#BBAA99', # dark border
    '#f9f6f0': '#F3E3D0', 
    '#f3efe5': '#EAE0D2',
    '#ece8dc': '#D2C4B4',
    '#fdfdfd': '#FAFAFA',
    '#e9e3d6': '#D2C4B4',
    
    # Primary (Teal -> Blue)
    '#8fbcae': '#81A6C6',
    '#6d988b': '#658AAB',
    '#b6e0d3': '#AACDDC',
    '#4c6f64': '#4A6984', # darker text on selected
    
    # rgba values (approximated strings)
    'rgba(143,188,174,0.3)': 'rgba(129,166,198,0.3)',
    'rgba(143,188,174,0.15)': 'rgba(129,166,198,0.15)',
    'rgba(143,188,174,0.08)': 'rgba(129,166,198,0.08)',
    'rgba(143,188,174,0.2)': 'rgba(129,166,198,0.2)',
    'rgba(143,188,174,0.12)': 'rgba(129,166,198,0.12)',
    'rgba(143,188,174,0.18)': 'rgba(129,166,198,0.18)',
    'rgba(143,188,174,0.25)': 'rgba(129,166,198,0.25)',
    'rgba(143,188,174,0.4)': 'rgba(129,166,198,0.4)',
    'rgba(143,188,174,0.5)': 'rgba(129,166,198,0.5)',
    'rgba(143,188,174,0.6)': 'rgba(129,166,198,0.6)',
    'rgba(109,152,139,0.3)': 'rgba(101,138,171,0.3)',
    
    # Texts (to softer blue-grays)
    '#3c4043': '#34495E',
    '#4a4d52': '#2C3E50',
    '#5f6368': '#4A5B6C',
    '#7d8287': '#6A7C8F',
    '#9aa0a6': '#8A9CAE',
    
    # Status
    '#88c096': '#AACDDC', # success to baby blue
    '#e29595': '#E09B9B', # keep red similar but softer
}

def replace_colors_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original_content = content
        
        for old_col, new_col in color_map.items():
            if old_col.startswith('#'):
                pattern = re.compile(re.escape(old_col), re.IGNORECASE)
                content = pattern.sub(new_col, content)
            else:
                content = content.replace(old_col, new_col)
                
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {filepath}")
    except Exception as e:
        print(f"Error processing {filepath}: {e}")

for root, dirs, files in os.walk('src'):
    for file in files:
        if file.endswith('.css') or file.endswith('.fxml') or file.endswith('.java'):
            replace_colors_in_file(os.path.join(root, file))

print("Done.")
