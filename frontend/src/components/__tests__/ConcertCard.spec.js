import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ConcertCard from '../ConcertCard.vue'

// Date far in the future so the countdown never shows "Past" during tests
const concert = {
  id: 'evt1',
  title: 'Metallica Live',
  venue: 'Olympiastadion',
  city: 'Berlin',
  date: '2999-08-14',
  time: '20:00:00',
  imageUrl: '',
  ticketUrl: '',
  status: 'onsale',
}

describe('ConcertCard', () => {
  it('zeigt Titel, Venue und Stadt des Konzerts an', () => {
    // Arrange + Act
    const wrapper = mount(ConcertCard, { props: { concert } })

    // Assert
    expect(wrapper.text()).toContain('Metallica Live')
    expect(wrapper.text()).toContain('Olympiastadion')
    expect(wrapper.text()).toContain('Berlin')
  })

  it('sendet ein delete-Event mit der Konzert-Id wenn das Loeschen bestaetigt wird', async () => {
    // Arrange
    const wrapper = mount(ConcertCard, { props: { concert } })

    // Act — click trash icon to show confirmation, then confirm
    await wrapper.find('.concert-card__delete').trigger('click')
    await wrapper.find('.concert-card__confirm-yes').trigger('click')

    // Assert
    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')[0]).toEqual(['evt1'])
  })

  it('bricht das Loeschen ab wenn Cancel gedrueckt wird', async () => {
    // Arrange
    const wrapper = mount(ConcertCard, { props: { concert } })

    // Act — click trash, then cancel
    await wrapper.find('.concert-card__delete').trigger('click')
    await wrapper.find('.concert-card__confirm-no').trigger('click')

    // Assert — no delete event emitted, trash icon visible again
    expect(wrapper.emitted('delete')).toBeFalsy()
    expect(wrapper.find('.concert-card__delete').exists()).toBe(true)
  })
})
