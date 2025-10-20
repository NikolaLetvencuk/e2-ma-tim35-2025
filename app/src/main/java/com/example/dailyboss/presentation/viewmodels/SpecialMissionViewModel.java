package com.example.dailyboss.presentation.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dailyboss.data.repository.SpecialMissionRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.MissionActivityLog;
import com.example.dailyboss.domain.model.SpecialMission;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

public class SpecialMissionViewModel extends AndroidViewModel {

    private final SpecialMissionRepository specialMissionRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<SpecialMission> _specialMission = new MutableLiveData<>();
    public LiveData<SpecialMission> specialMission = _specialMission;

    private final MutableLiveData<List<UserMissionProgress>> _allUserProgress = new MutableLiveData<>();
    public LiveData<List<UserMissionProgress>> allUserProgress = _allUserProgress;

    private final MutableLiveData<UserMissionProgress> _currentUserProgress = new MutableLiveData<>();
    public LiveData<UserMissionProgress> currentUserProgress = _currentUserProgress;

    private final MutableLiveData<List<MissionActivityLog>> _missionActivityLogs = new MutableLiveData<>();
    public LiveData<List<MissionActivityLog>> missionActivityLogs = _missionActivityLogs;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final String currentUserId;

    public SpecialMissionViewModel(@NonNull Application application) {
        super(application);
        specialMissionRepository = new SpecialMissionRepository(application.getApplicationContext());
        userRepository = new UserRepository(application.getApplicationContext());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public void loadSpecialMissionDetails(String missionId) {
        specialMissionRepository.getSpecialMissionById(missionId) // This already has local-first logic in repo
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        _specialMission.setValue(task.getResult());
                    } else {
                        String error = "Failed to load mission details.";
                        if (task.getException() instanceof FirebaseFirestoreException) {
                            error += " Error: " + task.getException().getMessage();
                        } else if (task.getException() != null) {
                            error += " General error: " + task.getException().getMessage();
                        }
                        _errorMessage.setValue(error);
                    }
                });
    }

    public void loadAllUserProgressForMission(String missionId) {
        specialMissionRepository.getAllUserProgressForMission(missionId) // This will now include local-first logic
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d("SpecialMissionViewModel", "Successfully loaded " + task.getResult().size() + " user progress items.");
                        _allUserProgress.setValue(task.getResult());
                    } else {
                        String error = "Failed to load all user progress.";
                        if (task.getException() != null) {
                            error += " General error: " + task.getException().getMessage();
                        }
                        _errorMessage.setValue(error);
                        Log.e("SpecialMissionViewModel", "Error loading all user progress: " + error, task.getException());
                    }
                });
    }

    public void loadCurrentUserProgressForMission(String missionId) {
        if (currentUserId == null) {
            _errorMessage.setValue("User not logged in.");
            return;
        }
        specialMissionRepository.getUserProgressForMission(missionId, currentUserId) // This will now include local-first logic
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        _currentUserProgress.setValue(task.getResult());
                    } else {
                        String error = "Failed to load current user progress.";
                        if (task.getException() instanceof FirebaseFirestoreException) {
                            error += " Error: " + task.getException().getMessage();
                        } else if (task.getException() != null) {
                            error += " General error: " + task.getException().getMessage();
                        }
                        _errorMessage.setValue(error);
                    }
                });
    }

    public void loadMissionActivityLogs(String missionId) {
        specialMissionRepository.getMissionActivityLogs(missionId) // This will now include local-first logic
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d("SpecialMissionViewModel", "Successfully loaded " + task.getResult().size() + " activity logs.");
                        _missionActivityLogs.setValue(task.getResult());
                    } else {
                        String error = "Failed to load mission activity logs.";
                        if (task.getException() != null) {
                            error += " General error: " + task.getException().getMessage();
                        }
                        _errorMessage.setValue(error);
                        Log.e("SpecialMissionViewModel", "Error loading mission activity logs: " + error, task.getException());
                    }
                });
    }

    public void updateMissionProgress(String specialMissionId, String activityType, int taskValue) {
        if (currentUserId == null) {
            _errorMessage.setValue("Korisnik nije prijavljen.");
            return;
        }

        // Dohvati username trenutnog korisnika - getLocalUser je sinhrona metoda
        User user = userRepository.getLocalUser(currentUserId);
        if (user != null) {
            String username = user.getUsername();
            if (username != null) {
                specialMissionRepository.updateMissionProgress(specialMissionId, currentUserId, username, activityType, taskValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Osveži podatke nakon uspešnog ažuriranja
                                loadSpecialMissionDetails(specialMissionId);
                                loadAllUserProgressForMission(specialMissionId);
                                loadCurrentUserProgressForMission(specialMissionId);
                                loadMissionActivityLogs(specialMissionId);
                            } else {
                                String error = "Greška pri ažuriranju progresa.";
                                if (task.getException() instanceof FirebaseFirestoreException) {
                                    error += " Greška: " + task.getException().getMessage();
                                } else if (task.getException() != null) {
                                    error += " Opšta greška: " + task.getException().getMessage();
                                }
                                _errorMessage.setValue(error);
                            }
                        });
            } else {
                _errorMessage.setValue("Korisničko ime nije pronađeno za prijavljenog korisnika.");
            }
        } else {
            _errorMessage.setValue("Neuspelo dohvatanje korisničkih podataka iz lokalne baze.");
        }
    }

    public void checkAndAwardMissionCompletion(String specialMissionId) {
        specialMissionRepository.getSpecialMissionById(specialMissionId).addOnCompleteListener(missionTask -> {
            if (missionTask.isSuccessful() && missionTask.getResult() != null) {
                SpecialMission mission = missionTask.getResult();
                if (mission.isCompletedSuccessfully() && !mission.isRewardsAwarded()) {
                    long nextRegularBossRewardCoins = 1000;
                    specialMissionRepository.getAllUserProgressForMission(specialMissionId)
                            .addOnCompleteListener(progressTask -> {
                                if (progressTask.isSuccessful() && progressTask.getResult() != null) {
                                    specialMissionRepository.awardSpecialMissionCompletion(specialMissionId, progressTask.getResult(), nextRegularBossRewardCoins)
                                            .addOnCompleteListener(awardTask -> {
                                                if (awardTask.isSuccessful()) {
                                                    _errorMessage.setValue("Nagrade za specijalnu misiju uspešno dodeljene!");
                                                    loadSpecialMissionDetails(specialMissionId);
                                                } else {
                                                    String error = "Greška pri dodeli nagrada.";
                                                    if (awardTask.getException() instanceof FirebaseFirestoreException) {
                                                        error += " Greška: " + awardTask.getException().getMessage();
                                                    } else if (awardTask.getException() != null) {
                                                        error += " Opšta greška: " + awardTask.getException().getMessage();
                                                    }
                                                    _errorMessage.setValue(error);
                                                }
                                            });
                                } else {
                                    String error = "Greška pri dohvatanju progresa za dodelu nagrada.";
                                    if (progressTask.getException() instanceof FirebaseFirestoreException) {
                                        error += " Greška: " + progressTask.getException().getMessage();
                                    } else if (progressTask.getException() != null) {
                                        error += " Opšta greška: " + progressTask.getException().getMessage();
                                    }
                                    _errorMessage.setValue(error);
                                }
                            });
                }
            } else {
                String error = "Greška pri proveri misije za dodelu nagrada.";
                if (missionTask.getException() instanceof FirebaseFirestoreException) {
                    error += " Greška: " + missionTask.getException().getMessage();
                } else if (missionTask.getException() != null) {
                    error += " Opšta greška: " + missionTask.getException().getMessage();
                }
                _errorMessage.setValue(error);
            }
        });
    }
}