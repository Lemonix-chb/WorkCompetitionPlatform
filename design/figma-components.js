// Figma 设计代码 - 可直接复制到Figma Plugin中运行
// 包含完整的三种角色界面设计

// ==================== 设计系统配置 ====================
const designSystem = {
  colors: {
    // 学生界面 - 活力蓝色系
    student: {
      primary: { r: 0.263, g: 0.518, b: 0.969, hex: '#4384F7' },
      secondary: { r: 0.902, g: 0.937, b: 1, hex: '#E5EFFF' },
      accent: { r: 0.118, g: 0.306, b: 0.592, hex: '#1E4E97' },
      success: { r: 0.22, g: 0.714, b: 0.416, hex: '#38B66A' },
      warning: { r: 0.961, g: 0.682, b: 0.184, hex: '#F5AE2F' },
      error: { r: 0.918, g: 0.29, b: 0.29, hex: '#EA4A4A' }
    },
    // 评审员界面 - 专业蓝灰色系
    judge: {
      primary: { r: 0.361, g: 0.447, b: 0.553, hex: '#5C728D' },
      secondary: { r: 0.941, g: 0.949, b: 0.957, hex: '#F0F2F4' },
      accent: { r: 0.227, g: 0.282, b: 0.353, hex: '#3A485A' }
    },
    // 管理员界面 - 深蓝色系
    admin: {
      primary: { r: 0.145, g: 0.204, b: 0.302, hex: '#25344D' },
      secondary: { r: 0.882, g: 0.902, b: 0.922, hex: '#E1E6EB' },
      accent: { r: 0.059, g: 0.102, b: 0.173, hex: '#0F1A2C' }
    },
    // 通用颜色
    white: { r: 1, g: 1, b: 1, hex: '#FFFFFF' },
    black: { r: 0, g: 0, b: 0, hex: '#000000' },
    gray: {
      50: { r: 0.98, g: 0.98, b: 0.98, hex: '#FAFAFA' },
      100: { r: 0.96, g: 0.96, b: 0.96, hex: '#F5F5F5' },
      200: { r: 0.9, g: 0.9, b: 0.9, hex: '#E5E5E5' },
      300: { r: 0.8, g: 0.8, b: 0.8, hex: '#CCCCCC' },
      400: { r: 0.6, g: 0.6, b: 0.6, hex: '#999999' },
      500: { r: 0.5, g: 0.5, b: 0.5, hex: '#808080' },
      600: { r: 0.4, g: 0.4, b: 0.4, hex: '#666666' },
      700: { r: 0.3, g: 0.3, b: 0.3, hex: '#4D4D4D' },
      800: { r: 0.2, g: 0.2, b: 0.2, hex: '#333333' },
      900: { r: 0.1, g: 0.1, b: 0.1, hex: '#1A1A1A' }
    }
  },
  typography: {
    fontFamily: 'Inter',
    fontSize: {
      xs: 12,
      sm: 14,
      base: 16,
      lg: 18,
      xl: 20,
      '2xl': 24,
      '3xl': 30,
      '4xl': 36,
      '5xl': 48
    },
    fontWeight: {
      regular: 400,
      medium: 500,
      semibold: 600,
      bold: 700
    },
    lineHeight: {
      tight: 1.25,
      normal: 1.5,
      relaxed: 1.75
    }
  },
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    '2xl': 48,
    '3xl': 64
  },
  borderRadius: {
    sm: 4,
    md: 8,
    lg: 12,
    xl: 16,
    '2xl': 24,
    full: 9999
  },
  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
  }
};

// ==================== 组件库 ====================

// 1. 按钮组件
function createButton(text, variant = 'primary', size = 'md') {
  const button = figma.createFrame();
  button.name = `Button/${variant}/${size}`;

  const colors = designSystem.colors;
  const typography = designSystem.typography;
  const borderRadius = designSystem.borderRadius;

  // 设置按钮样式
  let bgColor, textColor;
  switch(variant) {
    case 'primary':
      bgColor = colors.student.primary;
      textColor = colors.white;
      break;
    case 'secondary':
      bgColor = colors.student.secondary;
      textColor = colors.student.primary;
      break;
    case 'danger':
      bgColor = colors.student.error;
      textColor = colors.white;
      break;
  }

  button.fills = [{ type: 'SOLID', color: bgColor }];
  button.cornerRadius = borderRadius.md;

  // 设置尺寸
  let width, height, fontSize;
  switch(size) {
    case 'sm':
      width = 80;
      height = 32;
      fontSize = typography.fontSize.sm;
      break;
    case 'md':
      width = 120;
      height = 40;
      fontSize = typography.fontSize.base;
      break;
    case 'lg':
      width = 160;
      height = 48;
      fontSize = typography.fontSize.lg;
      break;
  }

  button.resize(width, height);

  // 添加文字
  const textNode = figma.createText();
  textNode.characters = text;
  textNode.fontSize = fontSize;
  textNode.fontName = { family: typography.fontFamily, style: 'Medium' };
  textNode.fills = [{ type: 'SOLID', color: textColor }];
  textNode.textAlignHorizontal = 'CENTER';
  textNode.textAlignVertical = 'CENTER';

  button.appendChild(textNode);

  return button;
}

// 2. 输入框组件
function createInput(label, placeholder = '请输入...') {
  const container = figma.createFrame();
  container.name = 'Input';
  container.layoutMode = 'VERTICAL';
  container.itemSpacing = 8;

  // 标签
  const labelNode = figma.createText();
  labelNode.characters = label;
  labelNode.fontSize = designSystem.typography.fontSize.sm;
  labelNode.fontName = { family: designSystem.typography.fontFamily, style: 'Medium' };
  labelNode.fills = [{ type: 'SOLID', color: designSystem.colors.gray[700] }];

  // 输入框
  const inputBox = figma.createFrame();
  inputBox.resize(360, 48);
  inputBox.fills = [{ type: 'SOLID', color: designSystem.colors.gray[100] }];
  inputBox.cornerRadius = designSystem.borderRadius.md;

  const placeholderNode = figma.createText();
  placeholderNode.characters = placeholder;
  placeholderNode.fontSize = designSystem.typography.fontSize.base;
  placeholderNode.fontName = { family: designSystem.typography.fontFamily, style: 'Regular' };
  placeholderNode.fills = [{ type: 'SOLID', color: designSystem.colors.gray[400] }];
  placeholderNode.x = 16;
  placeholderNode.y = 12;

  inputBox.appendChild(placeholderNode);

  container.appendChild(labelNode);
  container.appendChild(inputBox);

  return container;
}

// 3. 卡片组件
function createCard(title, content, variant = 'student') {
  const card = figma.createFrame();
  card.name = 'Card';
  card.layoutMode = 'VERTICAL';
  card.itemSpacing = 16;
  card.padding = { top: 24, right: 24, bottom: 24, left: 24 };

  // 设置圆角
  card.cornerRadius = variant === 'student' ? designSystem.borderRadius.xl : designSystem.borderRadius.lg;

  // 设置背景
  card.fills = [{ type: 'SOLID', color: designSystem.colors.white }];

  // 标题
  const titleNode = figma.createText();
  titleNode.characters = title;
  titleNode.fontSize = designSystem.typography.fontSize.xl;
  titleNode.fontName = { family: designSystem.typography.fontFamily, style: 'Bold' };
  titleNode.fills = [{ type: 'SOLID', color: designSystem.colors.gray[900] }];

  // 内容
  const contentNode = figma.createText();
  contentNode.characters = content;
  contentNode.fontSize = designSystem.typography.fontSize.base;
  contentNode.fontName = { family: designSystem.typography.fontFamily, style: 'Regular' };
  contentNode.fills = [{ type: 'SOLID', color: designSystem.colors.gray[600] }];

  card.appendChild(titleNode);
  card.appendChild(contentNode);

  return card;
}

// 4. 统计卡片组件
function createStatCard(value, label, icon, color) {
  const card = figma.createFrame();
  card.name = 'StatCard';
  card.resize(280, 160);
  card.fills = [{ type: 'SOLID', color: designSystem.colors.white }];
  card.cornerRadius = designSystem.borderRadius.xl;

  // 图标
  const iconNode = figma.createText();
  iconNode.characters = icon;
  iconNode.fontSize = 32;
  iconNode.x = 24;
  iconNode.y = 24;

  // 数值
  const valueNode = figma.createText();
  valueNode.characters = value;
  valueNode.fontSize = designSystem.typography.fontSize['5xl'];
  valueNode.fontName = { family: designSystem.typography.fontFamily, style: 'Bold' };
  valueNode.fills = [{ type: 'SOLID', color: color }];
  valueNode.x = 24;
  valueNode.y = 70;

  // 标签
  const labelNode = figma.createText();
  labelNode.characters = label;
  labelNode.fontSize = designSystem.typography.fontSize.base;
  labelNode.fontName = { family: designSystem.typography.fontFamily, style: 'Regular' };
  labelNode.fills = [{ type: 'SOLID', color: designSystem.colors.gray[500] }];
  labelNode.x = 24;
  labelNode.y = 125;

  card.appendChild(iconNode);
  card.appendChild(valueNode);
  card.appendChild(labelNode);

  return card;
}

// ==================== 页面生成函数 ====================

// 生成学生首页
async function createStudentHomePage() {
  await figma.loadFontAsync({ family: 'Inter', style: 'Regular' });
  await figma.loadFontAsync({ family: 'Inter', style: 'Medium' });
  await figma.loadFontAsync({ family: 'Inter', style: 'Bold' });

  const page = figma.createFrame();
  page.name = '学生-首页';
  page.resize(1440, 900);

  // 背景
  page.fills = [{ type: 'SOLID', color: designSystem.colors.gray[50] }];

  // 导航栏
  const navbar = createNavbar('student');
  page.appendChild(navbar);

  // Hero区域
  const hero = createHeroSection();
  hero.y = 64;
  page.appendChild(hero);

  // 统计卡片
  const statsContainer = figma.createFrame();
  statsContainer.name = '统计卡片';
  statsContainer.x = 80;
  statsContainer.y = 400;
  statsContainer.layoutMode = 'HORIZONTAL';
  statsContainer.itemSpacing = 24;

  const stats = [
    { value: '128', label: '参赛队伍', icon: '🏆', color: designSystem.colors.student.primary },
    { value: '356', label: '参赛学生', icon: '👥', color: designSystem.colors.student.success },
    { value: '98', label: '提交作品', icon: '📝', color: designSystem.colors.student.warning },
    { value: '24', label: '评审专家', icon: '⭐', color: designSystem.colors.student.accent }
  ];

  stats.forEach(stat => {
    const card = createStatCard(stat.value, stat.label, stat.icon, stat.color);
    statsContainer.appendChild(card);
  });

  page.appendChild(statsContainer);

  // 赛道卡片
  const tracksContainer = figma.createFrame();
  tracksContainer.name = '赛道介绍';
  tracksContainer.x = 80;
  tracksContainer.y = 600;

  const tracksTitle = figma.createText();
  tracksTitle.characters = '竞赛赛道';
  tracksTitle.fontSize = designSystem.typography.fontSize['3xl'];
  tracksTitle.fontName = { family: designSystem.typography.fontFamily, style: 'Bold' };
  tracksTitle.fills = [{ type: 'SOLID', color: designSystem.colors.gray[900] }];
  tracksContainer.appendChild(tracksTitle);

  figma.currentPage.appendChild(page);
  figma.viewport.scrollAndZoomIntoView([page]);

  return '学生首页已创建！';
}

// 创建导航栏
function createNavbar(variant) {
  const navbar = figma.createFrame();
  navbar.name = '导航栏';
  navbar.resize(1440, 64);
  navbar.fills = [{ type: 'SOLID', color: designSystem.colors.white }];

  // Logo
  const logo = figma.createText();
  logo.characters = '🏆 竞赛管理平台';
  logo.fontSize = designSystem.typography.fontSize.xl;
  logo.fontName = { family: designSystem.typography.fontFamily, style: 'Bold' };
  logo.fills = [{ type: 'SOLID', color: designSystem.colors.student.primary }];
  logo.x = 40;
  logo.y = 20;

  navbar.appendChild(logo);

  return navbar;
}

// 创建Hero区域
function createHeroSection() {
  const hero = figma.createFrame();
  hero.name = 'Hero区域';
  hero.resize(1440, 320);
  hero.fills = [{ type: 'SOLID', color: designSystem.colors.student.primary }];

  // 标题
  const title = figma.createText();
  title.characters = '2024年湖南农业大学计算机作品赛';
  title.fontSize = designSystem.typography.fontSize['5xl'];
  title.fontName = { family: designSystem.typography.fontFamily, style: 'Bold' };
  title.fills = [{ type: 'SOLID', color: designSystem.colors.white }];
  title.x = 80;
  title.y = 80;

  // 副标题
  const subtitle = figma.createText();
  subtitle.characters = '展现信息技术在多专业领域的创新应用，凸显学生跨学科融合能力';
  subtitle.fontSize = designSystem.typography.fontSize.lg;
  subtitle.fontName = { family: designSystem.typography.fontFamily, style: 'Regular' };
  subtitle.fills = [{ type: 'SOLID', color: designSystem.colors.white }];
  subtitle.x = 80;
  subtitle.y = 150;

  // CTA按钮
  const ctaButton = createButton('立即报名 →', 'primary', 'lg');
  ctaButton.x = 80;
  ctaButton.y = 220;

  hero.appendChild(title);
  hero.appendChild(subtitle);
  hero.appendChild(ctaButton);

  return hero;
}

// 执行创建学生首页
createStudentHomePage();