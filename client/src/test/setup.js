/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";

// The v-data-table-server is leading to the error "ReferenceError: ResizeObserver is not defined".
// Therefore, we have to mock the observer
// (https://stackoverflow.com/questions/68679993/referenceerror-resizeobserver-is-not-defined/77011294#77011294)
const ResizeObserverMock = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

vi.stubGlobal("ResizeObserver", ResizeObserverMock);
