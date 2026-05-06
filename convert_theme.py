import os

color_map = {
    # Backgrounds
    '#0d1b2a': '#fcfaf6',
    '#112233': '#f5f2eb',
    '#162032': '#ffffff',
    '#1e3048': '#e6e2d8',
    '#263d56': '#c9c3b8',
    '#07111c': '#f9f6f0',
    '#0a1a28': '#f3efe5',
    '#0d2035': '#ece8dc',
    '#0a1e2e': '#f3efe5',
    '#071525': '#ece8dc',
    '#1a2a3d': '#fdfdfd',
    
    # Teals
    '#14b8a6': '#8fbcae',
    '#0d9488': '#6d988b',
    '#99f6e4': '#b6e0d3',
    'rgba(20,184,166,0.3)': 'rgba(143,188,174,0.3)',
    'rgba(20,184,166,0.15)': 'rgba(143,188,174,0.15)',
    'rgba(20,184,166,0.08)': 'rgba(143,188,174,0.08)',
    'rgba(20,184,166,0.2)': 'rgba(143,188,174,0.2)',
    'rgba(20,184,166,0.12)': 'rgba(143,188,174,0.12)',
    'rgba(20,184,166,0.18)': 'rgba(143,188,174,0.18)',
    
    # Texts
    '#e2e8f0': '#3c4043',
    '#cbd5e1': '#4a4d52',
    '#94a3b8': '#5f6368',
    '#64748b': '#7d8287',
    '#475569': '#9aa0a6',
    '#334155': '#9aa0a6',
    
    # Status
    '#22c55e': '#88c096',
    '#f87171': '#e29595',
    '#b91c1c': '#e29595',
    'rgba(248,113,113,0.3)': 'rgba(226,149,149,0.3)',
    'rgba(248,113,113,0.1)': 'rgba(226,149,149,0.1)'
}

def replace_colors_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original_content = content
        
        # Case insensitive replace for hex colors
        import re
        for old_col, new_col in color_map.items():
            # For hex colors, make case insensitive
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
        if file.endswith('.fxml') or file.endswith('.java'):
            replace_colors_in_file(os.path.join(root, file))

print("Done.")
