import React, { useState, ChangeEvent, useEffect } from "react";
import {
  Calendar,
  momentLocalizer,
  SlotInfo,
  Event as CalendarEvent,
} from "react-big-calendar";

import { formatDateToISO } from "../../../utils/date";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import styles from "./EventForm.module.css";
import LoadingOverlay from "../../../ui/LoadingOverlay/LoadingOverlay";
import { EventResponse, Proposal } from "../../../models/api.models";
import { FeedbackMessage, FeedbackType } from "../../../models/feedback.model";
import Feedback from "../../../ui/Feedback/Feedback";
import useInput from "../../../hooks/use-input";
import useEventProposals from "../../../hooks/useEventProposals";
import useCalendar from "../../../utils/calendarUtils";
import calendarUtils from "../../../utils/calendarUtils";
const validateName = (name: string) =>
  name.trim().length >= 3 && name.trim().length <= 15;
const validateDescription = (description: string) =>
  description.trim().length >= 20 && description.trim().length <= 200;
const validateLocation = (location: string) =>
  location.trim().length >= 3 && location.trim().length <= 20;

const localizer = momentLocalizer(moment);
const EventForm: React.FC<{
  event?: EventResponse;
  onSubmit: (eventData: EventResponse) => void;
  isPending?: boolean;
  feedback?: FeedbackMessage | null;
  showFeedback?: (type: FeedbackType, message: string) => void;
  editable?: boolean;
}> = ({ event, onSubmit, isPending, feedback, showFeedback, editable }) => {
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const title = event ? "Edit Event" : "Create Event";

  const {
    value: nameValue,
    hasError: nameHasError,
    isValid: nameIsValid,
    valueChangeHandler: nameChanged,
    inputBlurHandler: nameBlurred,
    reset: resetName,
  } = useInput(validateName);

  const {
    value: descriptionValue,
    hasError: descriptionHasError,
    isValid: descriptionIsValid,
    valueChangeHandler: descriptionChanged,
    inputBlurHandler: descriptionBlurred,
    reset: resetDescription,
  } = useInput(validateDescription);

  const {
    value: locationValue,
    hasError: locationHasError,
    isValid: locationIsValid,
    valueChangeHandler: locationChanged,
    inputBlurHandler: locationBlurred,
    reset: resetLocation,
  } = useInput(validateLocation);

  const isSubmitDisabled =
    proposals.length === 0 ||
    !nameIsValid ||
    !descriptionIsValid ||
    !locationIsValid;

  useEventProposals(
    event,
    setProposals,
    locationChanged,
    descriptionChanged,
    nameChanged,
  );

  const { handleSelectSlot, handleSelectProposal } = calendarUtils(
    proposals,
    setProposals,
    event,
    showFeedback,
    editable,
  );

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const eventData: EventResponse = {
      id: event?.id || "",
      name: nameValue,
      description: descriptionValue,
      location: locationValue,
      eventProposals: proposals.map((proposal) => ({
        id: proposal.id,
        startDate: proposal.start.toISOString(),
      })),
    };
    //console.log("proposals", event?.eventProposals);
    onSubmit(eventData);
  };

  return (
    <section className={styles.createEventSection}>
      {isPending && <LoadingOverlay />}
      {feedback && (
        <Feedback
          feedback={feedback}
          clearFeedback={() => {
            showFeedback && showFeedback(null, "");
          }}
        />
      )}
      <h2 className={styles.title}>{title}</h2>
      <form onSubmit={handleSubmit} className={styles.formContainer}>
        <div className={styles.formHeader}>
          <input
            className={styles.textInput}
            type="text"
            value={nameValue}
            onChange={nameChanged}
            onBlur={nameBlurred}
            placeholder="Name"
          />
          {nameHasError && (
            <p className={styles.errorText}>
              Name must be between 3 and 15 characters long
            </p>
          )}

          <input
            className={styles.textInput}
            type="text"
            value={descriptionValue}
            onChange={descriptionChanged}
            onBlur={descriptionBlurred}
            placeholder="Description"
          />
          {descriptionHasError && (
            <p className={styles.errorText}>
              Description must be between 20 and 200 characters long
            </p>
          )}

          <input
            className={styles.textInput}
            type="text"
            value={locationValue}
            onChange={locationChanged}
            onBlur={locationBlurred}
            placeholder="Location"
          />
          {locationHasError && (
            <p className={styles.errorText}>
              Location must be between 3 and 20 characters long
            </p>
          )}
          <p className={styles.hint}>
            Click on a time slot in the calendar to add a time. Click on a time
            to remove it.
          </p>
        </div>
        <div className={styles.calendarContainer}>
          <Calendar
            selectable
            localizer={localizer}
            events={proposals}
            onSelectEvent={handleSelectProposal}
            onSelectSlot={handleSelectSlot}
            views={["week", "day"]}
            defaultView="week"
            onNavigate={(date) => console.log(date)}
          />
        </div>
        <button
          className={
            isSubmitDisabled ? styles.submitButtonDisabled : styles.submitButton
          }
          type="submit"
          disabled={isSubmitDisabled}
        >
          Submit
        </button>
      </form>
    </section>
  );
};

export default EventForm;
