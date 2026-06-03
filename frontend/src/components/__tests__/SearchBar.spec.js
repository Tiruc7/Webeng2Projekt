import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SearchBar from '../SearchBar.vue'

describe('SearchBar', () => {
  it('sendet ein search-Event mit dem eingegebenen Text', async () => {
    // Arrange
    const wrapper = mount(SearchBar)
    const input = wrapper.find('input.search__input')

    // Act — typing triggers @input which emits search
    await input.setValue('metallica')

    // Assert
    expect(wrapper.emitted('search')).toBeTruthy()
    const events = wrapper.emitted('search')
    expect(events[events.length - 1]).toEqual(['metallica'])
  })

  it('zeigt Vorschlaege an wenn der Input fokussiert wird', async () => {
    // Arrange
    const wrapper = mount(SearchBar, {
      props: { suggestions: ['Metallica', 'Slipknot', 'Tool'] },
    })

    // Act — focus opens the dropdown
    await wrapper.find('input.search__input').trigger('focus')

    // Assert
    const items = wrapper.findAll('.search__item')
    expect(items.length).toBeGreaterThan(0)
    expect(wrapper.text()).toContain('Metallica')
  })

  it('sendet ein select-Event wenn ein Vorschlag angeklickt wird', async () => {
    // Arrange
    const wrapper = mount(SearchBar, {
      props: { suggestions: ['Metallica', 'Slipknot', 'Tool'] },
    })
    await wrapper.find('input.search__input').trigger('focus')

    // Act — mousedown matches the @mousedown.prevent handler in the template
    await wrapper.findAll('.search__item')[0].trigger('mousedown')

    // Assert
    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')[0]).toEqual(['Metallica'])
  })
})
