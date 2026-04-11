import { h } from 'vue'

export const HomeIcon = {
  name: 'HomeIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('path', {
        d: 'M3 10L10 3L17 10V17H12V12H8V17H3V10Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round'
      })
    ])
  }
}

export const TeamIcon = {
  name: 'TeamIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 6, cy: 6, r: 3, stroke: 'currentColor', 'stroke-width': '2' }),
      h('circle', { cx: 14, cy: 6, r: 3, stroke: 'currentColor', 'stroke-width': '2' }),
      h('circle', { cx: 10, cy: 14, r: 3, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M6 9V10C6 11 7 12 10 12C13 12 14 11 14 10V9',
        stroke: 'currentColor',
        'stroke-width': '2'
      })
    ])
  }
}

export const WorkIcon = {
  name: 'WorkIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('rect', { x: 3, y: 3, width: 14, height: 14, rx: 2, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M7 7H13M7 10H13M7 13H10',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      })
    ])
  }
}

export const InviteIcon = {
  name: 'InviteIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 10, cy: 7, r: 5, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M4 17C4 14 7 12 10 12C13 12 16 14 16 17',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      }),
      h('path', {
        d: 'M16 7L19 7M19 7L19 4M19 7L19 10',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      })
    ])
  }
}

export const AwardIcon = {
  name: 'AwardIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 10, cy: 6, r: 5, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M7 11L5 19L10 16L15 19L13 11',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linejoin': 'round'
      })
    ])
  }
}

export const SettingsIcon = {
  name: 'SettingsIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 10, cy: 10, r: 3, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M10 3V5M10 15V17M17 10H15M5 10H3M15.5 4.5L14 6M6 14L4.5 15.5M15.5 15.5L14 14M6 6L4.5 4.5',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      })
    ])
  }
}

export const DoneIcon = {
  name: 'DoneIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 10, cy: 10, r: 8, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M7 10L9 12L13 8',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round'
      })
    ])
  }
}

export const StatsIcon = {
  name: 'StatsIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('rect', { x: 3, y: 12, width: 3, height: 5, rx: 1, stroke: 'currentColor', 'stroke-width': '2' }),
      h('rect', { x: 8, y: 8, width: 3, height: 9, rx: 1, stroke: 'currentColor', 'stroke-width': '2' }),
      h('rect', { x: 13, y: 4, width: 3, height: 13, rx: 1, stroke: 'currentColor', 'stroke-width': '2' })
    ])
  }
}

export const CompIcon = {
  name: 'CompIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('rect', { x: 3, y: 3, width: 14, height: 14, rx: 2, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M7 8H13M7 11H13M7 14H11',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      })
    ])
  }
}

export const UserIcon = {
  name: 'UserIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('circle', { cx: 10, cy: 7, r: 5, stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', {
        d: 'M4 17C4 14 7 12 10 12C13 12 16 14 16 17',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round'
      })
    ])
  }
}

export const ReviewIcon = {
  name: 'ReviewIcon',
  render() {
    return h('svg', { width: 20, height: 20, viewBox: '0 0 20 20', fill: 'none' }, [
      h('path', {
        d: 'M3 5L10 2L17 5V11C17 15 13 18 10 18C7 18 3 15 3 11V5Z',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linejoin': 'round'
      }),
      h('path', {
        d: 'M7 10L9 12L13 8',
        stroke: 'currentColor',
        'stroke-width': '2',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round'
      })
    ])
  }
}